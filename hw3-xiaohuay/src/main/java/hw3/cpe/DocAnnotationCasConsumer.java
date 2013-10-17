package hw3.cpe;

import hw3.util.UimaConvenience;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;

public class DocAnnotationCasConsumer extends CasConsumer_ImplBase {
  int documentCount;

  double totalScore;

  public void initialize() throws ResourceInitializationException {
    documentCount = 0;
    totalScore = 0;
  }

  public void processCas(CAS aCAS) throws ResourceProcessException {
    JCas aJCas;
    try {
      aJCas = aCAS.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    // retrieve the filename of the input file from the CAS

    FSIterator<Annotation> it = aJCas.getAnnotationIndex(SourceDocumentInformation.type).iterator();
    if (it.hasNext()) {
      List<Answer> answers = UimaConvenience.getAnnotationList(aJCas, Answer.class);
      Question question = JCasUtil.selectSingle(aJCas, Question.class);
      double totalCorrect = 0.0;

      for (Answer answer : answers) {
        if (answer.getIsCorrect()) {
          totalCorrect += 1;
        }
      }
      // sort the answers based on their confidence (score)
      Collections.sort(answers, new Comparator<Answer>() {
        public int compare(Answer ans1, Answer ans2) {
          return ans1.getConfidence() > ans2.getConfidence() ? -1 : 1;
        }
      });

      int numCorrect = 0;
      for (int i = 0; i < totalCorrect; i++) {
        if (answers.get(i).getIsCorrect()) {
          numCorrect++;
        }
      }

      double precisionAtN = numCorrect / totalCorrect;
      // output the precision in the console
      System.out.println(String.format("Question: %s", question.getCoveredText()));

      for (Answer answer : answers) {
        String correctInd = answer.getIsCorrect() ? "+" : "-";
        System.out.println(String.format("%s %.2f %s", correctInd, answer.getConfidence(),
                answer.getCoveredText()));
      }

      System.out.println(String.format("Precision at %d: %.2f ", (int) totalCorrect, precisionAtN));
      System.out.println();

      totalScore += precisionAtN;
      documentCount += 1;
    }

  }

  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {
    // no default behavior
    System.out.println(String.format("Average precision: %.2f ", totalScore / documentCount));
  }
}
