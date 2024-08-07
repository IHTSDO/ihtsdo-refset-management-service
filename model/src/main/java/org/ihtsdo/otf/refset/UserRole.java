/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

/**
 * The Enum UserRole.
 *
 */
public enum UserRole {

  /** The viewer. */
  VIEWER("Viewer"),

  /** The author. */
  AUTHOR("Author"),
  
  /** The reviewer. */
  REVIEWER("Reviewer"),
  
  /** The second reviewer. */
  REVIEWER2("Reviewer2"),
  
  /**  The user. */
  USER("User"),
  
  /** The lead. */
  LEAD("Lead"),
  
  /** The administrator. */
  ADMIN("Admin");

  /** The value. */
  private String value;

  /**
   * Instantiates a {@link UserRole} from the specified parameters.
   *
   * @param value the value
   */
  private UserRole(String value) {
    this.value = value;
  }

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Checks for privileges of.
   *
   * @param role the role
   * @return true, if successful
   */
  public boolean hasPrivilegesOf(UserRole role) {
    if (this == UserRole.VIEWER && role == UserRole.VIEWER)
      return true;
    else if (this == UserRole.AUTHOR
        && (role == UserRole.VIEWER || role == UserRole.AUTHOR))
      return true;
    else if (this == UserRole.REVIEWER
        && (role == UserRole.VIEWER || role == UserRole.USER || role == UserRole.AUTHOR || role == UserRole.REVIEWER))
      return true;
    else if (this == UserRole.REVIEWER2
        && (role == UserRole.VIEWER || role == UserRole.USER || role == UserRole.AUTHOR || role == UserRole.REVIEWER || role == UserRole.REVIEWER2))
      return true;
    else if (this == UserRole.USER
        && (role == UserRole.VIEWER || role == UserRole.USER || role == UserRole.AUTHOR))
      return true;
    else if (this == UserRole.LEAD
        && (role == UserRole.VIEWER || role == UserRole.USER || role == UserRole.AUTHOR || role == UserRole.REVIEWER || role == UserRole.REVIEWER2 || role == UserRole.LEAD))
      return true;
    else if (this == UserRole.ADMIN)
      return true;
    else
      return false;
  }
}
