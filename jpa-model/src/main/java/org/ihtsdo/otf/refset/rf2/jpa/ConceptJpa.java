/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.ihtsdo.otf.refset.rf2.AssociationReferenceConceptRefSetMember;
import org.ihtsdo.otf.refset.rf2.AttributeValueConceptRefSetMember;
import org.ihtsdo.otf.refset.rf2.ComplexMapRefSetMember;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.rf2.SimpleMapRefSetMember;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;

/**
 * Jpa enabled implementation of {@link Concept}.
 */
@Entity
// @UniqueConstraint here is being used to create an index, not to enforce
// uniqueness
@Table(name = "concepts", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@Indexed
@AnalyzerDef(name = "noStopWord", tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class), filters = {
    @TokenFilterDef(factory = StandardFilterFactory.class),
    @TokenFilterDef(factory = LowerCaseFilterFactory.class)
})
@XmlRootElement(name = "concept")
public class ConceptJpa extends AbstractComponent implements Concept {

  /** The workflow status. */
  @Column(nullable = true)
  private String workflowStatus;

  /** The definition status id. */
  @Column(nullable = false)
  private String definitionStatusId;

  /** The anonymous flag. */
  @Column(nullable = false)
  private boolean anonymous = false;

  /** The fully defined flag. */
  @Column(nullable = false)
  private boolean fullyDefined = false;

  /** The descriptions. */
  @OneToMany(mappedBy = "concept", orphanRemoval = true, targetEntity = DescriptionJpa.class)
  @IndexedEmbedded(targetElement = DescriptionJpa.class)
  private List<Description> descriptions = null;

  /** The relationships. */
  @OneToMany(mappedBy = "sourceConcept", orphanRemoval = true, targetEntity = RelationshipJpa.class)
  private List<Relationship> relationships = null;

  /** The child count. */
  @Transient
  private int childCount = -1;

  /** The simple RefSet members. */
  @OneToMany(mappedBy = "concept", targetEntity = SimpleRefSetMemberJpa.class)
  private List<SimpleRefSetMember> simpleRefSetMembers = null;

  /** The simpleMap RefSet members. */
  @OneToMany(mappedBy = "concept", targetEntity = SimpleMapRefSetMemberJpa.class)
  private List<SimpleMapRefSetMember> simpleMapRefSetMembers = null;

  /** The complexMap RefSet members. */
  @OneToMany(mappedBy = "concept", targetEntity = ComplexMapRefSetMemberJpa.class)
  private List<ComplexMapRefSetMember> complexMapRefSetMembers = null;

  /** The attributeValue RefSet members. */
  @OneToMany(mappedBy = "concept", targetEntity = AttributeValueConceptRefSetMemberJpa.class)
  private List<AttributeValueConceptRefSetMember> attributeValueRefSetMembers =
      null;

  /** The associationReference RefSet members. */
  @OneToMany(mappedBy = "concept", orphanRemoval = true, targetEntity = AssociationReferenceConceptRefSetMemberJpa.class)
  private List<AssociationReferenceConceptRefSetMember> associationReferenceRefSetMembers =
      new ArrayList<>();

  /** The default preferred name. */
  @Column(nullable = false, length = 256)
  private String defaultPreferredName;

  /**
   * Instantiates an empty {@link ConceptJpa}.
   */
  public ConceptJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptJpa} from the specified parameters.
   *
   * @param concept the concept
   * @param deepCopy the deep copy flag
   */
  public ConceptJpa(Concept concept, boolean deepCopy) {
    super(concept);
    defaultPreferredName = concept.getDefaultPreferredName();
    definitionStatusId = concept.getDefinitionStatusId();
    workflowStatus = concept.getWorkflowStatus();

    if (deepCopy) {
      descriptions = new ArrayList<>();
      for (Description description : concept.getDescriptions()) {
        Description newDescription = new DescriptionJpa(description, deepCopy);
        newDescription.setConcept(this);
        descriptions.add(newDescription);
      }
      relationships = new ArrayList<>();
      for (Relationship rel : concept.getRelationships()) {
        Relationship newRel = new RelationshipJpa(rel);
        newRel.setSourceConcept(this);
        relationships.add(newRel);
      }
      attributeValueRefSetMembers = new ArrayList<>();
      for (AttributeValueConceptRefSetMember member : concept
          .getAttributeValueRefSetMembers()) {
        AttributeValueConceptRefSetMember newMember =
            new AttributeValueConceptRefSetMemberJpa(member);
        newMember.setConcept(this);
        attributeValueRefSetMembers.add(newMember);
      }

      associationReferenceRefSetMembers = new ArrayList<>();
      for (AssociationReferenceConceptRefSetMember member : concept
          .getAssociationReferenceRefSetMembers()) {
        AssociationReferenceConceptRefSetMember newMember =
            new AssociationReferenceConceptRefSetMemberJpa(member);
        newMember.setConcept(this);
        associationReferenceRefSetMembers.add(newMember);
      }

      complexMapRefSetMembers = new ArrayList<>();
      for (ComplexMapRefSetMember member : concept.getComplexMapRefSetMembers()) {
        ComplexMapRefSetMember newMember =
            new ComplexMapRefSetMemberJpa(member);
        newMember.setConcept(this);
        complexMapRefSetMembers.add(newMember);
      }

      simpleMapRefSetMembers = new ArrayList<>();
      for (SimpleMapRefSetMember member : concept.getSimpleMapRefSetMembers()) {
        SimpleMapRefSetMember newMember = new SimpleMapRefSetMemberJpa(member);
        newMember.setConcept(this);
        simpleMapRefSetMembers.add(newMember);
      }

      simpleRefSetMembers = new ArrayList<>();
      for (SimpleRefSetMember member : concept.getSimpleRefSetMembers()) {
        SimpleRefSetMember newMember = new SimpleRefSetMemberJpa(member);
        newMember.setConcept(this);
        simpleRefSetMembers.add(newMember);
      }

    }
  }

  @Override
  public String getWorkflowStatus() {
    return workflowStatus;
  }

  @Override
  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  @Override
  public String getDefinitionStatusId() {
    return definitionStatusId;
  }

  @Override
  public void setDefinitionStatusId(String definitionStatusId) {
    this.definitionStatusId = definitionStatusId;
  }

  @Override
  public boolean isAnonymous() {
    return anonymous;
  }

  @Override
  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  @Override
  public boolean isFullyDefined() {
    return fullyDefined;
  }

  @Override
  public void setFullyDefined(boolean fullyDefined) {
    this.fullyDefined = fullyDefined;
  }

  @Override
  @XmlElement(type = DescriptionJpa.class)
  public List<Description> getDescriptions() {
    if (descriptions == null) {
      descriptions = new ArrayList<>();
    }
    return descriptions;
  }

  @Override
  public void setDescriptions(List<Description> descriptions) {
    if (descriptions != null) {
      this.descriptions = new ArrayList<>();
      for (Description description : descriptions) {
        description.setConcept(this);
      }
      this.descriptions.addAll(descriptions);
    }
  }

  @Override
  public void addDescription(Description description) {
    if (descriptions == null) {
      descriptions = new ArrayList<>();
    }
    description.setConcept(this);
    descriptions.add(description);
  }

  @Override
  public void removeDescription(Description description) {
    if (descriptions == null) {
      return;
    }
    descriptions.remove(description);
  }

  @Override
  @XmlElement(type = RelationshipJpa.class)
  public List<Relationship> getRelationships() {
    if (relationships == null) {
      relationships = new ArrayList<>();
    }
    return relationships;
  }

  @Override
  public void addRelationship(Relationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>();
    }
    relationship.setSourceConcept(this);
    relationships.add(relationship);
  }

  @Override
  public void removeRelationship(Relationship relationship) {
    if (relationships == null) {
      return;
    }
    relationships.remove(relationship);
  }

  @Override
  public void setRelationships(List<Relationship> relationships) {
    if (relationships != null) {
      this.relationships = new ArrayList<>();
      for (Relationship relationship : relationships) {
        relationship.setSourceConcept(this);
      }
      this.relationships.addAll(relationships);
    }
  }

  @XmlTransient
  @Override
  public List<SimpleRefSetMember> getSimpleRefSetMembers() {
    if (simpleRefSetMembers == null) {
      simpleRefSetMembers = new ArrayList<>();
    }
    return simpleRefSetMembers;
  }

  @Override
  public void setSimpleRefSetMembers(
    List<SimpleRefSetMember> simpleRefSetMembers) {
    if (simpleRefSetMembers != null) {
      this.simpleRefSetMembers = new ArrayList<>();
      for (SimpleRefSetMember member : simpleRefSetMembers) {
        member.setConcept(this);
      }
      this.simpleRefSetMembers.addAll(simpleRefSetMembers);
    }
  }

  @Override
  public void addSimpleRefSetMember(SimpleRefSetMember simpleRefSetMember) {
    if (simpleRefSetMembers == null) {
      simpleRefSetMembers = new ArrayList<>();
    }
    simpleRefSetMember.setConcept(this);
    simpleRefSetMembers.add(simpleRefSetMember);
  }

  @Override
  public void removeSimpleRefSetMember(SimpleRefSetMember simpleRefSetMember) {
    if (simpleRefSetMembers == null) {
      return;
    }
    simpleRefSetMembers.remove(simpleRefSetMember);
  }

  @XmlTransient
  @Override
  public List<SimpleMapRefSetMember> getSimpleMapRefSetMembers() {
    if (simpleMapRefSetMembers == null) {
      simpleMapRefSetMembers = new ArrayList<>();
    }
    return simpleMapRefSetMembers;
  }

  @Override
  public void setSimpleMapRefSetMembers(
    List<SimpleMapRefSetMember> simpleMapRefSetMembers) {
    if (simpleMapRefSetMembers != null) {
      this.simpleMapRefSetMembers = new ArrayList<>();
      for (SimpleMapRefSetMember member : simpleMapRefSetMembers) {
        member.setConcept(this);
      }
      this.simpleMapRefSetMembers.addAll(simpleMapRefSetMembers);
    }
  }

  @Override
  public void addSimpleMapRefSetMember(
    SimpleMapRefSetMember simpleMapRefSetMember) {
    if (simpleMapRefSetMembers == null) {
      simpleMapRefSetMembers = new ArrayList<>();
    }
    simpleMapRefSetMember.setConcept(this);
    simpleMapRefSetMembers.add(simpleMapRefSetMember);
  }

  @Override
  public void removeSimpleMapRefSetMember(
    SimpleMapRefSetMember simpleMapRefSetMember) {
    if (simpleMapRefSetMembers == null) {
      return;
    }
    simpleMapRefSetMembers.remove(simpleMapRefSetMember);
  }

  @XmlTransient
  @Override
  public List<ComplexMapRefSetMember> getComplexMapRefSetMembers() {
    if (complexMapRefSetMembers == null) {
      complexMapRefSetMembers = new ArrayList<>();
    }
    return complexMapRefSetMembers;
  }

  @Override
  public void setComplexMapRefSetMembers(
    List<ComplexMapRefSetMember> complexMapRefSetMembers) {
    if (complexMapRefSetMembers != null) {
      this.complexMapRefSetMembers = new ArrayList<>();
      for (ComplexMapRefSetMember member : complexMapRefSetMembers) {
        member.setConcept(this);
      }
      this.complexMapRefSetMembers.addAll(complexMapRefSetMembers);
    }
  }

  @Override
  public void addComplexMapRefSetMember(
    ComplexMapRefSetMember complexMapRefSetMember) {
    if (complexMapRefSetMembers == null) {
      complexMapRefSetMembers = new ArrayList<>();
    }
    complexMapRefSetMember.setConcept(this);
    complexMapRefSetMembers.add(complexMapRefSetMember);
  }

  @Override
  public void removeComplexMapRefSetMember(
    ComplexMapRefSetMember complexMapRefSetMember) {
    if (complexMapRefSetMembers == null) {
      return;
    }
    complexMapRefSetMembers.remove(complexMapRefSetMember);
  }

  @XmlTransient
  @Override
  public List<AttributeValueConceptRefSetMember> getAttributeValueRefSetMembers() {
    if (attributeValueRefSetMembers == null) {
      attributeValueRefSetMembers = new ArrayList<>();
    }
    return attributeValueRefSetMembers;
  }

  @Override
  public void setAttributeValueRefSetMembers(
    List<AttributeValueConceptRefSetMember> attributeValueRefSetMembers) {
    if (attributeValueRefSetMembers != null) {
      this.attributeValueRefSetMembers = new ArrayList<>();
      for (AttributeValueConceptRefSetMember member : attributeValueRefSetMembers) {
        member.setConcept(this);
      }
      this.attributeValueRefSetMembers.addAll(attributeValueRefSetMembers);
    }
  }

  @Override
  public void addAttributeValueRefSetMember(
    AttributeValueConceptRefSetMember attributeValueRefSetMember) {
    if (attributeValueRefSetMembers == null) {
      attributeValueRefSetMembers = new ArrayList<>();
    }
    attributeValueRefSetMember.setConcept(this);
    attributeValueRefSetMembers.add(attributeValueRefSetMember);
  }

  @Override
  public void removeAttributeValueRefSetMember(
    AttributeValueConceptRefSetMember attributeValueRefSetMember) {
    if (attributeValueRefSetMembers == null) {
      return;
    }
    attributeValueRefSetMembers.remove(attributeValueRefSetMember);
  }

  @XmlTransient
  @Override
  public List<AssociationReferenceConceptRefSetMember> getAssociationReferenceRefSetMembers() {
    if (associationReferenceRefSetMembers == null) {
      associationReferenceRefSetMembers = new ArrayList<>();
    }
    return associationReferenceRefSetMembers;
  }

  @Override
  public void setAssociationReferenceRefSetMembers(
    List<AssociationReferenceConceptRefSetMember> associationReferenceRefSetMembers) {
    if (associationReferenceRefSetMembers != null) {
      this.associationReferenceRefSetMembers = new ArrayList<>();
      for (AssociationReferenceConceptRefSetMember member : associationReferenceRefSetMembers) {
        member.setConcept(this);
      }
      this.associationReferenceRefSetMembers
          .addAll(associationReferenceRefSetMembers);
    }
  }

  @Override
  public void addAssociationReferenceRefSetMember(
    AssociationReferenceConceptRefSetMember associationReferenceRefSetMember) {
    if (associationReferenceRefSetMembers == null) {
      associationReferenceRefSetMembers = new ArrayList<>();
    }
    associationReferenceRefSetMember.setConcept(this);
    associationReferenceRefSetMembers.add(associationReferenceRefSetMember);
  }

  @Override
  public void removeAssociationReferenceRefSetMember(
    AssociationReferenceConceptRefSetMember associationReferenceRefSetMember) {
    if (associationReferenceRefSetMembers == null) {
      return;
    }
    associationReferenceRefSetMembers.remove(associationReferenceRefSetMember);
  }

  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "defaultPreferredNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @Analyzer(definition = "noStopWord")
  public String getDefaultPreferredName() {
    return defaultPreferredName;
  }

  @Override
  public void setDefaultPreferredName(String defaultPreferredName) {
    this.defaultPreferredName = defaultPreferredName;
  }

  @Override
  public String toString() {
    return super.toString() + ", " + getDefinitionStatusId() + ","
        + getDefaultPreferredName();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ConceptJpa other = (ConceptJpa) obj;
    if (definitionStatusId == null) {
      if (other.definitionStatusId != null)
        return false;
    } else if (!definitionStatusId.equals(other.definitionStatusId))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((definitionStatusId == null) ? 0 : definitionStatusId.hashCode());
    return result;
  }
}