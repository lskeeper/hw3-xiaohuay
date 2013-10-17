package hw3.annotators;

import hw3.util.UimaConvenience;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Annotation;
import edu.cmu.deiis.types.Question;
import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Sentence;
import edu.cmu.deiis.types.Token;

/**
 * 
 * @author xiaohua
 * 
 *         This annotator annotates each sentence and the tokens in each sentence using regular
 *         expressions that match English tokens. And the confidence for either sentence or token is
 *         set to 1.
 * 
 */
public class TokenAndSentenceAnnotator extends JCasAnnotator_ImplBase {
  /**
   * @see JCasAnnotator_ImplBase#process(JCas)
   */

  private Pattern tokenPattern = Pattern.compile("[\\w\'-]+|$*\\d+\\.\\d+");

  public void process(JCas aJCas) {
    Question question = JCasUtil.selectSingle(aJCas, Question.class);
    List<Answer> answers = UimaConvenience.getAnnotationList(aJCas, Answer.class);

    makeAnnotations(aJCas, question);
    for (Answer answer : answers) {
      makeAnnotations(aJCas, answer);
    }

  }

  private void makeAnnotations(JCas aJCas, Annotation annotation) {
    Sentence sentence = new Sentence(aJCas, annotation.getBegin(), annotation.getEnd());
    sentence.setCasProcessorId(this.getClass().getName());
    sentence.addToIndexes();
    String sentText = sentence.getCoveredText();
    Matcher matcher = tokenPattern.matcher(sentText);
    int offset = annotation.getBegin();
    while (matcher.find()) {
      Token token = new Token(aJCas);
      token.setBegin(matcher.start() + offset);
      token.setEnd(matcher.end() + offset);
      token.setConfidence(1);
      token.setCasProcessorId(this.getClass().getName());
      token.addToIndexes();
    }
  }
}
