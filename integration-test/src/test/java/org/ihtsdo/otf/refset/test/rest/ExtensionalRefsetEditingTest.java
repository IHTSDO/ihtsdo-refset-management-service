package org.ihtsdo.otf.refset.test.rest;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.junit.Test;

public class ExtensionalRefsetEditingTest extends RefsetTest {

  /**
   * Test refset - creation, addition, adding members, removing members,
   * changing desc, remove refset
   *
   * @throws Exception the exception
   */
  @Test
  public void testRefset001() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testRefset001");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    // Create refset (EXTENSIONAL)
    RefsetJpa refset1 =
        makeRefset("refset99", "needs definition", Refset.Type.EXTENSIONAL,
            project2, null);

    // Validate refset
    ValidationResult result =
        validationService.validateRefset(refset1, adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset
    RefsetJpa newRefset =
        (RefsetJpa) refsetService.addRefset(refset1, adminAuthToken);
    if (!newRefset.equals(refset1)) {
      throw new Exception("Refset does not pass equality test.");
    }

    // Add 5 members to refset
    ConceptRefsetMemberJpa member1 =
        makeConceptRefsetMember("member1", "123", newRefset);
    refsetService.addRefsetMember(member1, adminAuthToken);
    ConceptRefsetMemberJpa member2 =
        makeConceptRefsetMember("member2", "12344", newRefset);
    refsetService.addRefsetMember(member2, adminAuthToken);
    ConceptRefsetMemberJpa member3 =
        makeConceptRefsetMember("member3", "123333", newRefset);
    refsetService.addRefsetMember(member3, adminAuthToken);
    ConceptRefsetMemberJpa member4 =
        makeConceptRefsetMember("member4", "123223", newRefset);
    refsetService.addRefsetMember(member4, adminAuthToken);
    ConceptRefsetMemberJpa member5 =
        makeConceptRefsetMember("member5", "1234545", newRefset);
    refsetService.addRefsetMember(member5, adminAuthToken);

    if (refsetService
        .findRefsetMembersForQuery(newRefset.getId(), "",
            new PfsParameterJpa(), adminAuthToken).getObjects().size() != 5) {
      throw new Exception("Refset did not pass the add refset members test.");
    }

    // Remove 2 members
    refsetService.removeRefsetMember(member5.getId(), adminAuthToken);
    refsetService.removeRefsetMember(member4.getId(), adminAuthToken);

    if (refsetService
        .findRefsetMembersForQuery(newRefset.getId(), "",
            new PfsParameterJpa(), adminAuthToken).getObjects().size() != 3) {
      throw new Exception("Refset did not pass the remove refset members test.");
    }

    newRefset.setDefinition("new definition");

    refsetService.updateRefset(newRefset, adminAuthToken);

    // remove refset
    refsetService.removeRefset(newRefset.getId(), true, adminAuthToken);

    if (refsetService.getRefset(newRefset.getId(), adminAuthToken) != null) {
      throw new Exception("Refset did not pass the remove refset test.");
    }
  }

}
