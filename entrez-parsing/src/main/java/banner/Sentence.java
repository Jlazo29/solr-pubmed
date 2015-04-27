/* 
 Copyright (c) 2007 Arizona State University, Dept. of Computer Science and Dept. of Biomedical Informatics.
 This file is part of the BANNER Named Entity Recognition System, http://banner.sourceforge.net
 This software is provided under the terms of the Common Public License, version 1.0, as published by http://www.opensource.org.  For further information, see the file 'LICENSE.txt' included with this distribution.
 */

package banner;

import java.util.*;

import banner.tagging.Mention;
import banner.tagging.MentionType;
import banner.tagging.TaggedToken;
import banner.tagging.Tagger;
import banner.tagging.TaggedToken.TagFormat;
import banner.tagging.TaggedToken.TagPosition;
import banner.tokenization.Token;
import banner.tokenization.Tokenizer;
import banner.tokenization.WhitespaceTokenizer;

/**
 * This class represents a single sentence, and provides for the text to be
 * tokenized and for mentions.
 * 
 * @author Bob
 */
public class Sentence
{

	private String tag;
	private String text;
	private List<Token> tokens;
	private List<Mention> mentions;

	private Sentence()
	{
		tokens = new ArrayList<Token>();
		mentions = new ArrayList<Mention>();
	}

	/**
	 * Creates a new {@link Sentence} with the specified text and a
	 * <code>null</code> tag.
	 * 
	 * @param text
	 *            The text of the sentence
	 */
	public Sentence(String text)
	{
		this(null, text);
	}

	/**
	 * Creates a new {@link Sentence} with the specified tag and text
	 * 
	 * @param tag
	 *            The tag for the {@link Sentence}, which may be a unique
	 *            identifier
	 * @param text
	 *            The text of the sentence
	 */
	public Sentence(String tag, String text)
	{
		this.tag = tag;
		if (text == null)
			throw new IllegalArgumentException("Text cannot be null");
		text = text.trim();
		if (text.length() == 0)
			throw new IllegalArgumentException("Text must have length greater than 0");
		this.text = text;
		tokens = new ArrayList<Token>();
		mentions = new ArrayList<Mention>();
	}

	public static Sentence loadFromPiped(String tag, String pipedText)
	{
		Sentence sentence = new Sentence();
		sentence.tag = tag;
		String[] taggedTokens = pipedText.split("\\s+");
		StringBuffer sentenceText = new StringBuffer();
		TagPosition[] positions = new TagPosition[taggedTokens.length];
		MentionType[] types = new MentionType[taggedTokens.length];
		for (int i = 0; i < taggedTokens.length; i++)
		{
			String[] tokenSplit = taggedTokens[i].split("\\|");
			sentenceText.append(tokenSplit[0]);
			sentenceText.append(" ");
			// The tag string is e.g. "O" or "B-GENE"
			String[] tagSplit = tokenSplit[1].split("-");
			positions[i] = TagPosition.valueOf(tagSplit[0]);
			// TODO Verify that the type stays the same
			if (tagSplit.length == 2)
				types[i] = MentionType.getType(tagSplit[1]);
		}
		sentence.text = sentenceText.toString().trim();
		(new WhitespaceTokenizer()).tokenize(sentence);
		sentence.addMentions(positions, types);
		return sentence;
	}

	public static Sentence loadFromXML(String tag, String xmlText)
	{
		Sentence sentence = new Sentence();
		sentence.tag = tag;
		String[] pieces = xmlText.split("\\s+");
		StringBuffer sentenceText = new StringBuffer();
		for (int i = 0; i < pieces.length; i++)
		{
			if (!pieces[i].startsWith("<") || !pieces[i].endsWith(">"))
			{
				sentenceText.append(pieces[i]);
				sentenceText.append(" ");
			}
		}
		sentence.text = sentenceText.toString().trim();
		(new WhitespaceTokenizer()).tokenize(sentence);
		int index = 0;
		int mentionStart = -1;
		MentionType mentionType = null;
		for (int i = 0; i < pieces.length; i++)
		{
			if (pieces[i].startsWith("<") && pieces[i].endsWith(">"))
			{
				String typeStr = pieces[i];
				if (typeStr.startsWith("</"))
				{
					if (mentionStart == -1 || mentionType == null)
						throw new IllegalArgumentException("");
					Mention mention = new Mention(sentence, mentionType, mentionStart, index);
					sentence.mentions.add(mention);
					mentionStart = -1;
					mentionType = null;
				} else
				{
					if (mentionStart != -1 || mentionType != null)
						throw new IllegalArgumentException("");
					mentionStart = index;
					typeStr = typeStr.replace(">", "");
					typeStr = typeStr.replace("<", "");
					mentionType = MentionType.getType(typeStr);
				}
			} else
			{
				index++;
			}
		}
		return sentence;
	}

	public void inferTokenization(String tokenizedText)
	{
		if (tokens.size() != 0)
			throw new IllegalStateException();
		String[] split = tokenizedText.split("\\s+");
		int start = 0;
		for (int i = 0; i < split.length; i++)
		{
			while (Character.isWhitespace(text.charAt(start)))
				start++;
			if (!text.substring(start, start + split[i].length()).equals(split[i]))
				throw new IllegalArgumentException();
			tokens.add(new Token(this, start, start + split[i].length()));
			start += split[i].length();
		}
	}

	/**
	 * Adds a {@link Token} to this {@link Sentence}. Normally called by
	 * instances of {@link Tokenizer}.
	 * 
	 * @param token
	 */
	public void addToken(Token token)
	{
		// Add verification of no token overlap
		if (!token.getSentence().equals(this))
			throw new IllegalArgumentException();
		tokens.add(token);
	}

	/**
	 * Adds a {@link Mention} to this Sentence, ignoring any potential overlap
	 * with existing {@link Mention}s. Normally called by instance of
	 * {@link Tagger} or post-processors.
	 * 
	 * @param mention
	 */
	public void addMention(Mention mention)
	{
		if (!mention.getSentence().equals(this))
			throw new IllegalArgumentException();
		mentions.add(mention);
	}

	/**
	 * Adds a {@link Mention} to this Sentence or merges the {@link Mention}
	 * into an existing {@link Mention} which the new one would overlap.
	 * Normally called by instance of {@link Tagger} or post-processors.
	 * 
	 * @param mention
	 */
	public void addOrMergeMention(Mention mention)
	{
		if (!mention.getSentence().equals(this))
			throw new IllegalArgumentException();
		List<Mention> overlapping = new ArrayList<Mention>();
		for (Mention mention2 : mentions)
		{
			if (mention.overlaps(mention2) && mention.getType().equals(mention2.getType()))
				overlapping.add(mention2);
		}
		if (overlapping.size() == 0)
		{
			mentions.add(mention);
		} else
		{
			for (Mention mention2 : overlapping)
			{
				mentions.remove(mention2);
				mentions.add(new Mention(this, mention.getType(), Math.min(mention.getStart(), mention2.getStart()), Math.max(mention.getEnd(), mention2.getEnd())));
			}
		}
	}

	public boolean removeMention(Mention mention)
	{
		return mentions.remove(mention);
	}

    @Override
    public String toString() {
        return "Sentence{" +
                "tag='" + tag + '\'' +
                ", text='" + text + '\'' +
                ", tokens=" + tokens +
                ", mentions=" + mentions +
                '}';
    }

    public void addMentions(TagPosition[] positions, MentionType[] types)
	{
		// TODO Verify correct transitions & type continuity
		if (tokens.size() != positions.length)
			throw new IllegalArgumentException();
		if (tokens.size() != types.length)
			throw new IllegalArgumentException();
		int startIndex = -1;
		for (int i = 0; i < positions.length; i++)
		{
			if (positions[i] == TagPosition.O)
			{
				if (startIndex != -1)
					addOrMergeMention(new Mention(this, types[i - 1], startIndex, i));
				startIndex = -1;
			} else if (positions[i] == TagPosition.B)
			{
				if (startIndex != -1)
					addOrMergeMention(new Mention(this, types[i - 1], startIndex, i));
				startIndex = i;
			} else if (positions[i] == TagPosition.W)
			{
				if (startIndex != -1)
					addOrMergeMention(new Mention(this, types[i - 1], startIndex, i));
				startIndex = i;
			} else
			{
				if (startIndex == -1)
					startIndex = i;
			}
		}
	}

	/**
	 * @return the tag for the {@link Sentence}
	 */
	public String getTag()
	{
		return tag;
	}

	/**
	 * @return The text of the {@link Sentence}
	 */
	public String getText()
	{
		return text + " ";
	}

	public int getTokenIndex(int charIndex, boolean returnNextIfBoundary)
	{
		// Find:
		// The token with the highest start that is below the given character
		// index
		// The token with the lowest end that is above the given index
		int startToken = -1;
		int endToken = -1;
		for (int i = 0; i < tokens.size(); i++)
		{
			Token token = tokens.get(i);
			if (token.getStart() <= charIndex)
				if (startToken == -1 || tokens.get(startToken).getStart() <= token.getStart())
					startToken = i;
			if (token.getEnd() > charIndex)
				if (endToken == -1 || tokens.get(endToken).getEnd() > token.getEnd())
					endToken = i;
		}
		if (returnNextIfBoundary)
			return startToken;
		else
			return endToken;
	}

	/**
	 * @return The tokenized form of the text for this {@link Sentence}. Formed
	 *         by placing a single space character between the text for each
	 *         token in the {@link Sentence}.
	 */
	public String getTokenizedText()
	{
		StringBuffer text2 = new StringBuffer();
		for (Token token : tokens)
		{
			text2.append(token.getText());
			text2.append(" ");
		}
		return text2.toString().trim();
	}

	public String getText(int start, int end)
	{
		return text.substring(start, end);
	}

	/**
	 * @return The {@link List} of {@link Token}s for this {@link Sentence}
	 */
	public List<Token> getTokens()
	{
		return Collections.unmodifiableList(tokens);
	}

	/**
	 * @return The {@link List} of {@link Token}s for this {@link Sentence}
	 */
	public List<String> getTokenText()
	{
		List<String> tokenTexts = new ArrayList<String>();
		for (Token t : tokens)
			tokenTexts.add(t.getText());
		return tokenTexts;
	}

	/**
	 * @return The {@link List} of {@link Mention}s for this {@link Sentence}.
	 *         This list may or may not contain overlaps
	 */
	public List<Mention> getMentions()
	{
		return Collections.unmodifiableList(mentions);
	}

	public Set<String> getMentionText(){
		HashSet<String> result = new HashSet<>();
		for (Mention mention : mentions){
			result.add(mention.getText());
		}
		return result;
	}

	/**
	 * Assumes that each token is tagged either 0 or 1 times
	 */
	private Mention getMention(int tokenIndex)
	{
		Mention mention = null;
		for (Mention mention2 : mentions)
		{
			if (mention2.contains(tokenIndex))
				if (mention == null)
					mention = mention2;
				else
				{
					// System.out.println(mention.getText() + " " +
					// mention.getStart() + "-" + mention.getEnd());
					// System.out.println(mention2.getText() + " " +
					// mention2.getStart() + "-" + mention2.getEnd());
					String message = "Token \"" + tokens.get(tokenIndex).getText() + "\" (" + tokenIndex + ") is tagged multiple times";
					throw new IllegalArgumentException(message);
				}
		}
		return mention;
	}

	public List<TaggedToken> getTaggedTokens()
	{
		List<TaggedToken> taggedTokens = new ArrayList<TaggedToken>();
		for (int i = 0; i < tokens.size(); i++)
		{
			taggedTokens.add(new TaggedToken(this, getMention(i), i));
		}
		return Collections.unmodifiableList(taggedTokens);
	}

	/**
	 * Returns a text representation of the tagging for this {@link Sentence},
	 * using the specified {@link TagFormat}. In other words, each token in the
	 * sentence is given a tag indicating its position in a mention or that the
	 * token is not a mention.
	 * 
	 * @param format
	 *            The {@link TagFormat} to use
	 * @return A text representation of the tagging for this {@link Sentence},
	 *         using the specified {@link TagFormat}
	 */
	public String getTrainingText(TagFormat format)
	{
		return getTrainingText(format, false);
	}

	/**
	 * Returns a text representation of the tagging for this {@link Sentence},
	 * using the specified {@link TagFormat}. In other words, each token in the
	 * sentence is given a tag indicating its position in a mention or that the
	 * token is not a mention. Assumes that each token is tagged either 0 or 1
	 * times.
	 * 
	 * @param format
	 *            The {@link TagFormat} to use
	 * @param reverse
	 *            Whether to return the text in reverse order
	 * @return A text representation of the tagging for this {@link Sentence},
	 *         using the specified {@link TagFormat}
	 */
	public String getTrainingText(TagFormat format, boolean reverse)
	{
		List<TaggedToken> taggedTokens = new ArrayList<TaggedToken>(getTaggedTokens());
		if (reverse)
			Collections.reverse(taggedTokens);
		StringBuffer trainingText = new StringBuffer();
		for (TaggedToken token : taggedTokens)
		{
			trainingText.append(token.getText(format));
			trainingText.append(" ");
		}
		return trainingText.toString().trim();
	}

	public List<Mention> getMentions(Token token)
	{
		ArrayList<Mention> mentionsForToken = new ArrayList<Mention>();
		for (Mention mention : mentions)
			if (mention.getTokens().contains(token))
				mentionsForToken.add(mention);
		return Collections.unmodifiableList(mentionsForToken);
	}

	public List<String> getTokenLabels(TagFormat format)
	{
		List<String> labels = new ArrayList<String>();
		for (int i = 0; i < tokens.size(); i++)
		{
			List<Mention> tokenMentions = getMentions(tokens.get(i));
			if (tokenMentions.size() == 0)
				labels.add(TagPosition.O.name());
			else if (tokenMentions.size() == 1)
				labels.add(TagPosition.getPositionText(format, tokenMentions.get(0), i));
			else
			{
				StringBuilder label = new StringBuilder();
				Iterator<Mention> mentionIterator = tokenMentions.iterator();
				label.append(mentionIterator.next());
				while (mentionIterator.hasNext())
				{
					label.append("&");
					label.append(mentionIterator.next());
				}
				labels.add(label.toString());
			}
		}
		return Collections.unmodifiableList(labels);
	}

	/**
	 * Returns a SGML/XML representation of the tagged sentence. Mentions are
	 * surrounded by opening and closing tags containing the mention type.
	 * Assumes that each token is tagged either 0 or 1 times.
	 * 
	 * @return A SGML/XML representation of the tagged sentence
	 */
	public String getSGML()
	{
		List<TaggedToken> taggedTokens = getTaggedTokens();
		StringBuffer text2 = new StringBuffer();
		for (int i = 0; i < taggedTokens.size(); i++) {
			TaggedToken token = taggedTokens.get(i);
			TagPosition position = token.getPosition(TagFormat.IOBEW);
			if (position == TagPosition.B || position == TagPosition.W)
				text2.append("<" + token.getMention().getType().getText() + "> ");
			text2.append(token.getToken().getText() + " ");
			if (position == TagPosition.E || position == TagPosition.W)
				text2.append("</" + token.getMention().getType().getText() + "> ");
		}
		return text2.toString().trim();
	}

    /**
     * Returns an HTML representation of the tagged sentence for highlighting purposes.
     * Mentions are surrounded by opening and closing HTML span-with-class tags containing the mention type.
     * Assumes that each token is tagged either 0 or 1 times.
     *
     * @return A SGML/XML representation of the tagged sentence
     */
    public String getHTMLtext() {

        List<TaggedToken> taggedTokens = getTaggedTokens();
        StringBuilder text2 = new StringBuilder();
        for (int i = 0; i < taggedTokens.size(); i++)
        {
            TaggedToken token = taggedTokens.get(i);
            TagPosition position = token.getPosition(TagFormat.IOBEW);
            if (position == TagPosition.B || position == TagPosition.W)
                text2.append("<span class=\"" + token.getMention().getType().getText() + "\"> ");
            text2.append(token.getToken().getText() + " ");
            if (position == TagPosition.E || position == TagPosition.W)
                text2.append("</span> ");
        }
        return text2.toString();
    }


	// ----- Object overrides -----

	@Override
	public int hashCode()
	{
		return text.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Sentence other = (Sentence) obj;
		if (text == null)
		{
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}
