package hw3.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
/**
 * @author Chris Welty
 * 
 *         Required JAR files:
 *         <ul>
 *         </ul>
 * 
 *         Version History:
 *         <ul>
 *         <li>0.1 [Apr 20, 2004]: Created.
 *         </ul>
 * 
 * @version: 0.1
 */

public class UimaConvenience {
  /**
   * Returns a list of annotations of the specified type in the specified CAS. The difference of
   * this with JCasUtil.select is that this method will store everything into a list first, so if
   * modifying stuff, it won't raise ConcurrentModificationException
   * 
   * @param aJCas
   * @param clazz
   * @return a list of annotations of the specified type in the specified CAS
   */
  public static <T extends TOP> List<T> getAnnotationList(JCas aJCas, final Class<T> clazz) {
    try {
      // TODO: The integer field 'type' and 'typeIndexID' take the same value as those of its parent
      // type. We need to fix the code below so it gets only the specified annotation.
      final int type = clazz.getField("type").getInt(clazz);

      List<T> annotationList = new ArrayList<T>();
      Iterator<?> annotationIter = aJCas.getJFSIndexRepository().getAllIndexedFS(type);
      while (annotationIter.hasNext()) {
        @SuppressWarnings("unchecked")
        T annotation = (T) annotationIter.next();
        annotationList.add(annotation);
      }
      return annotationList;
    } catch (SecurityException e) {
      throw new CASRuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new CASRuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new CASRuntimeException(e);
    }
  }

  
  
}