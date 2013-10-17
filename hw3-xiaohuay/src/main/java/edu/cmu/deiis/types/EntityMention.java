

/* First created by JCasGen Mon Oct 07 15:07:06 EDT 2013 */
package edu.cmu.deiis.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Mon Oct 07 15:07:06 EDT 2013
 * XML source: /Users/xiaohua/git/hw3-xiaohuay/hw3-xiaohuay/src/main/resources/descriptors/deiis_types.xml
 * @generated */
public class EntityMention extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(EntityMention.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected EntityMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public EntityMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public EntityMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public EntityMention(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: entityType

  /** getter for entityType - gets 
   * @generated */
  public String getEntityType() {
    if (EntityMention_Type.featOkTst && ((EntityMention_Type)jcasType).casFeat_entityType == null)
      jcasType.jcas.throwFeatMissing("entityType", "edu.cmu.deiis.types.EntityMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((EntityMention_Type)jcasType).casFeatCode_entityType);}
    
  /** setter for entityType - sets  
   * @generated */
  public void setEntityType(String v) {
    if (EntityMention_Type.featOkTst && ((EntityMention_Type)jcasType).casFeat_entityType == null)
      jcasType.jcas.throwFeatMissing("entityType", "edu.cmu.deiis.types.EntityMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((EntityMention_Type)jcasType).casFeatCode_entityType, v);}    
  }

    