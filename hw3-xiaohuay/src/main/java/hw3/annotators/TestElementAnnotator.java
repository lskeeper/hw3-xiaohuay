package hw3.annotators;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;

/**
 * 
 * @author xiaohua
 * This annotator annotates each question and answer in the document.
 * The confidence for each annotation is set to 1.
 *
 */
public class TestElementAnnotator extends JCasAnnotator_ImplBase {
  /**
   * @see JCasAnnotator_ImplBase#process(JCas)
   */
  public void process(JCas aJCas) {
    // get document text
    String docText = aJCas.getDocumentText();
    // get each row of the document text
    String[] textLines = docText.split("\n");
    int offset = 0;
    for (String line : textLines) {
      // get the content of each question or answer by parsing the non-whitespace text
      String targetText = line.substring(1, line.length()).trim();
      // get the start index
      int startIndex = line.indexOf(targetText.charAt(0)) + offset;
      // get the end index
      int endIndex = line.lastIndexOf(targetText.charAt(targetText.length() - 1)) + offset + 1;
      if (line.startsWith("Q")) {
        // found one question -- create question annotation
        Question question = new Question(aJCas);
        question.setBegin(startIndex);
        question.setEnd(endIndex);
        question.setCasProcessorId(this.getClass().getName());
        question.setConfidence(1);
        question.addToIndexes();
      } else if (line.startsWith("A")) {
        // found one answer -- create question annotation
        Answer answer = new Answer(aJCas);
        if (targetText.startsWith("1")) {
          answer.setIsCorrect(true);
        } else {
          answer.setIsCorrect(false);
        }
        String answerText = targetText.substring(1, targetText.length()).trim();
        answer.setBegin(line.indexOf(answerText.charAt(0)) + offset);
        answer.setEnd(line.lastIndexOf(answerText.charAt(answerText.length() - 1)) + offset + 1);
        answer.setCasProcessorId(this.getClass().getName());
        answer.addToIndexes();
      }
      offset += line.length() + 1;
    }
  }
}
