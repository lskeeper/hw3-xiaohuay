package hw3.annotators;

import hw3.util.UimaConvenience;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.token.type.Token;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.cmu.deiis.types.Annotation;
import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.NGram;
import edu.cmu.deiis.types.Question;

/**
 * 
 * @author xiaohua
 * 
 *         This annotator generates a score for each answer annotation, based on token overlap,
 *         n-gram overlap, and named entities overlap scores measured by cosine similarity. The
 *         weights for token overlap, n-gram overlap and NE overlap are 0.7, 0.2 and 0.1
 *         respectively.
 * 
 * 
 */
public class ScoreAnnotator extends JCasAnnotator_ImplBase {
  double finalScore = 0.0;

  double documentCount = 0;

  public void initialize(UimaContext aContext) {

  }

  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    Question question = JCasUtil.selectSingle(aJCas, Question.class);
    Map<String, Integer> questionTokenCountMap = getCoveredTypeCounts(question, Token.class, null);
    Map<String, Integer> questionNECountMap = getCoveredTypeCounts(question,
            NamedEntityMention.class, "mentionType");
    Map<String, Integer> questionLemmaCountMap = getCoveredTypeCounts(question, Token.class, "lemma");
    Map<String, Integer> questionPOSCountMap = getCoveredTypeCounts(question, Token.class, "pos");
    Table<Integer, String, Integer> questionNGramCountMap = getCoveredNGramCounts(question);
    for (Answer answer : UimaConvenience.getAnnotationList(aJCas, Answer.class)) {
      Map<String, Integer> answerTokenCountMap = getCoveredTypeCounts(answer, Token.class, null);
      Map<String, Integer> answerNECountMap = getCoveredTypeCounts(answer,
              NamedEntityMention.class, "mentionType");
      Map<String, Integer> answerLemmaCountMap = getCoveredTypeCounts(answer, Token.class, "lemma");
      Map<String, Integer> answerPOSCountMap = getCoveredTypeCounts(answer, Token.class, "pos");
      Table<Integer, String, Integer> answerNGramCountMap = getCoveredNGramCounts(answer);

      double namedEntityOverlapScore = getCosine(questionNECountMap, answerNECountMap);
      double tokenOverlapScore = getCosine(questionTokenCountMap, answerTokenCountMap);
      double nGramOverlapScore = getNGramScore(questionNGramCountMap, answerNGramCountMap);
      double lemmaOverlapScore = getCosine(questionLemmaCountMap, answerLemmaCountMap);
      double posOverlapScore = getCosine(questionPOSCountMap, answerPOSCountMap);
      finalScore = 0.5 * tokenOverlapScore + 0.2 * nGramOverlapScore + 0.1
              * namedEntityOverlapScore + 0.1 * lemmaOverlapScore + 0.1 * posOverlapScore;

      AnswerScore answerScore = new AnswerScore(aJCas);
      answerScore.setAnswer(answer);
      answerScore.setScore(finalScore);
      answer.setConfidence(finalScore);
      answerScore.setCasProcessorId(this.getClass().getName());
      answerScore.addToIndexes();
    }
  }

  /**
   * Get the cosine similarity between two n-gram strings, which is the arithmetic average of all
   * n-gram pairs
   * 
   * @param questionNGramCountMap
   * @param answerNGramCountMap
   * @return the cosine similarity between two bags of n-grams in each annotation
   */
  private double getNGramScore(Table<Integer, String, Integer> questionNGramCountMap,
          Table<Integer, String, Integer> answerNGramCountMap) {
    int allNGramsCount = 0;
    double nGramOverlapScore = 0;
    // get the (n-gram, frequency) map
    Map<Integer, Map<String, Integer>> questionNGramRows = questionNGramCountMap.rowMap();
    for (Entry<Integer, Map<String, Integer>> answerNGramEntry : answerNGramCountMap.rowMap()
            .entrySet()) {

      Integer key = answerNGramEntry.getKey();
      double nGramScore = getCosine(questionNGramRows.get(key), answerNGramEntry.getValue());
      allNGramsCount++;
      nGramOverlapScore += nGramScore;
    }
    // avoid devide-by-0 error
    if (allNGramsCount > 0) {
      return nGramOverlapScore / allNGramsCount;
    } else {
      return 0;
    }
  }

  /**
   * Get the cosine similarity between two bag of words
   * 
   * @param bag1
   * @param bag2
   * @return The cosine similarity between two bags of words
   * 
   */
  private double getCosine(Map<String, Integer> bag1, Map<String, Integer> bag2) {
    if (bag1.isEmpty() || bag2.isEmpty()) {
      return 0;
    }

    double score = 0.0;
    for (Entry<String, Integer> tokenEntry : bag1.entrySet()) {
      String tokenString = tokenEntry.getKey();
      Integer count = tokenEntry.getValue();
      if (bag2.containsKey(tokenString)) {
        score += bag2.get(tokenString) * count;
      }
    }
    return score / Math.sqrt(getLength(bag1) * getLength(bag2));
  }

  /**
   * Get the Euclidean length of a bag of word
   * 
   * @param bag
   * @return The Euclidean length computed from the (word, frequency) map
   * 
   */
  private double getLength(Map<String, Integer> bag) {
    double result = 0;
    for (Entry<String, Integer> tokenEntry : bag.entrySet()) {
      Integer count = tokenEntry.getValue();
      result += count * count;
    }
    return result;
  }

  /**
   * Get the (ngram, frequency) map of the ngrams covered in the given annotation
   * 
   * @param annotation
   * @return The (ngram, frequency) map of the ngrams covered in the given annotation
   */

  private <T extends Annotation> Table<Integer, String, Integer> getCoveredNGramCounts(T annotation) {
    Table<Integer, String, Integer> nGramCountMap = HashBasedTable.create();
    for (NGram nGram : JCasUtil.selectCovered(NGram.class, annotation)) {
      Integer N = nGram.getElements().size();
      if (N == 1) {
        continue;
      }
      String nGramText = nGram.getCoveredText();
      if (nGramCountMap.contains(N, nGramText)) {

        nGramCountMap.put(N, nGramText, nGramCountMap.get(N, nGramText) + 1);
      } else {

        nGramCountMap.put(N, nGramText, 1);
      }
    }
    return nGramCountMap;
  }

  /**
   * Get the (word, frequency) map for the give annotation
   * 
   * @param annotation
   * @param clazz
   * @return The (word, frequency) map for the give annotation
   */
  private <A extends Annotation, T extends AnnotationFS> Map<String, Integer> getCoveredTypeCounts(
          A annotation, Class<T> clazz, String featureName) {
    Map<String, Integer> typeCountMap = new HashMap<String, Integer>();
    for (T typeInstance : JCasUtil.selectCovered(clazz, annotation)) {
      String keyString = null;
      if (featureName != null) {
        Type annotationType = typeInstance.getType();
        try {
          keyString = typeInstance.getFeatureValueAsString(annotationType
                  .getFeatureByBaseName(featureName));
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        keyString = typeInstance.getCoveredText();
      }
      if (keyString != null && Pattern.matches("\\p{Punct}", keyString)) {
        continue;
      }
      if (typeCountMap.containsKey(keyString)) {
        typeCountMap.put(keyString, typeCountMap.get(keyString) + 1);
      } else {
        typeCountMap.put(keyString, 1);
      }
    }
    return typeCountMap;
  }

}
