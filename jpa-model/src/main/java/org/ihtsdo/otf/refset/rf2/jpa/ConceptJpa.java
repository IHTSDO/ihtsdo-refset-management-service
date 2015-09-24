/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

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
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowStatus workflowStatus = WorkflowStatus.NEW;

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

  /** The default preferred name. */
  @Column(nullable = false, length = 256)
  private String name;

  /** The translation. */
  @ManyToOne(targetEntity = TranslationJpa.class, optional = false)
  @ContainedIn
  private Translation translation;

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
    name = concept.getName();
    definitionStatusId = concept.getDefinitionStatusId();
    workflowStatus = concept.getWorkflowStatus();
    translation = concept.getTranslation();

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

    }
  }

  /* see superclass */
  @Override
  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  /* see superclass */
  @Override
  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  /* see superclass */
  @Override
  public String getDefinitionStatusId() {
    return definitionStatusId;
  }

  /* see superclass */
  @Override
  public void setDefinitionStatusId(String definitionStatusId) {
    this.definitionStatusId = definitionStatusId;
  }

  /* see superclass */
  @Override
  public boolean isAnonymous() {
    return anonymous;
  }

  /* see superclass */
  @Override
  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  /* see superclass */
  @Override
  public boolean isFullyDefined() {
    return fullyDefined;
  }

  /* see superclass */
  @Override
  public void setFullyDefined(boolean fullyDefined) {
    this.fullyDefined = fullyDefined;
  }

  /* see superclass */
  @Override
  @XmlElement(type = DescriptionJpa.class)
  public List<Description> getDescriptions() {
    if (descriptions == null) {
      descriptions = new ArrayList<>();
    }
    return descriptions;
  }

  /* see superclass */
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

  /* see superclass */
  @Override
  public void addDescription(Description description) {
    if (descriptions == null) {
      descriptions = new ArrayList<>();
    }
    description.setConcept(this);
    descriptions.add(description);
  }

  /* see superclass */
  @Override
  public void removeDescription(Description description) {
    if (descriptions == null) {
      return;
    }
    descriptions.remove(description);
  }

  /* see superclass */
  @Override
  @XmlElement(type = RelationshipJpa.class)
  public List<Relationship> getRelationships() {
    if (relationships == null) {
      relationships = new ArrayList<>();
    }
    return relationships;
  }

  /* see superclass */
  @Override
  public void addRelationship(Relationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>();
    }
    relationship.setSourceConcept(this);
    relationships.add(relationship);
  }

  /* see superclass */
  @Override
  public void removeRelationship(Relationship relationship) {
    if (relationships == null) {
      return;
    }
    relationships.remove(relationship);
  }

  /* see superclass */
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

  /* see superclass */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "defaultPreferredNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @Analyzer(definition = "noStopWord")
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String defaultPreferredName) {
    this.name = defaultPreferredName;
  }

  /* see superclass */
  @Override
  public Translation getTranslation() {
    return translation;
  }

  /* see superclass */
  @Override
  public void setTranslation(Translation translation) {
    this.translation = translation;
  }

  /* see superclass */
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

  /* see superclass */
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

  /* see superclass */
  @Override
  public String toString() {
    return "ConceptJpa [workflowStatus=" + workflowStatus
        + ", definitionStatusId=" + definitionStatusId + ", anonymous="
        + anonymous + ", fullyDefined=" + fullyDefined + ", descriptions="
        + descriptions + ", relationships=" + relationships + ", childCount="
        + childCount + ", name=" + name + ", translation=" + translation + "]";
  }

}