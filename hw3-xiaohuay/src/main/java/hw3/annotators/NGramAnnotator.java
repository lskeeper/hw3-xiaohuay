package hw3.annotators;

import hw3.util.UimaConvenience;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.deiis.types.NGram;
import edu.cmu.deiis.types.Sentence;
import edu.cmu.deiis.types.Token;

/**
 * 
 * @author xiaohua
 * 
 *         This annotator annotates the n-grams in each question and answer. The n-gram tokens are
 *         stored in the FSArray data structures, each element of which is a single token. The
 *         confidence for each annotation is set to 1.
 * 
 */

public class NGramAnnotator extends JCasAnnotator_ImplBase {

  Integer[] orderList;

  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    // get the order of n-grams
    orderList = (Integer[]) aContext.getConfigParameterValue("N");
  }

  public void process(JCas aJCas) {

    for (Sentence sentence : UimaConvenience.getAnnotationList(aJCas, Sentence.class)) {
      // get the covered tokens in the target sentence
      List<Token> tokenList = JCasUtil.selectCovered(Token.class, sentence);
      for (int startTokenIndex = 0; startTokenIndex < tokenList.size(); startTokenIndex++) {
        for (int i = 0; i < orderList.length; i++) {
          int N = orderList[i];
          int endTokenIndex = startTokenIndex + N - 1;
          if (endTokenIndex < tokenList.size()) {
            // put the nGram annotation into CAS
            NGram nGram = new NGram(aJCas, tokenList.get(startTokenIndex).getBegin(), tokenList
                    .get(endTokenIndex).getEnd());
            // set the elements of the FSArray
            nGram.setElements(FSCollectionFactory.createFSArray(aJCas,
                    JCasUtil.selectCovered(Token.class, nGram)));
            nGram.setElementType(Token.class.getSimpleName());
            nGram.setConfidence(1);
            nGram.setCasProcessorId(this.getClass().getName());
            nGram.addToIndexes();
          }
        }
      }
    }
  }
}
