package com.waves_rsp.ikb4stream.producer.score.sample;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class NLP {

    /**
     * Define NLP properties
     * @return Properties
     */
    private static Properties setProperties() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos");
        props.setProperty("tokenize.language", "fr");
        props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/french/french.tagger");
        return props;
    }

    /**
     * Uses specified properties to get sentences
     * @param tweetPost
     * @return List<CoreMap>
     */
    private static List<CoreMap> getSentences(String tweetPost) {
        // creates a StanfordCoreNLP object, with POS tagging, parsing, and coreference resolution
        StanfordCoreNLP pipeline = new StanfordCoreNLP(setProperties());
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(tweetPost);
        // run all Annotators on this text
        pipeline.annotate(document);
        return document.get(CoreAnnotations.SentencesAnnotation.class);
    }

    /**
     * Tokenize (NLP) the tweet and keep only noun and verbs in the Map
     * @param sentences
     * @return Map<String, String> contains nouns and verbs from the tweet
     */
    private static Map<String, String> sentencesToPOSMap(List<CoreMap> sentences) {
        Map<String, String> posMap = new HashMap<String, String>();
        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if((pos.contains("N") || pos.contains("V")) && !pos.contains("PUNC")) {
                    posMap.put(word, pos);
                }
            }
        }
        return posMap;
    }

    /**
     * Apply the NLP algorithm on a tweet. Keep only distinct words from the tweet.
     * @param tweetPost
     * @return Map<String, String> contains nouns and verbs from the tweet
     */
    public static Map<String, String> applyNLPtoTweet(String tweetPost) {
        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = getSentences(tweetPost);
        return sentencesToPOSMap(sentences);
    }

}