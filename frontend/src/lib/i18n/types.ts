import type { ErrorCode } from '@/types/error-codes.types'

/**
 * Supported languages
 */
export type Language = 'en' | 'km'

/**
 * Translation key types for error code messages
 */
export type ErrorCodeTranslations = Record<ErrorCode, string>

/**
 * UI text translations structure
 */
export interface UITranslations {
  // Auth pages
  auth: {
    signIn: {
      title: string
      subtitle: string
      emailOrPhone: string
      emailOrPhonePlaceholder: string
      password: string
      passwordPlaceholder: string
      academicYear: string
      academicYearPlaceholder: string
      forgotPassword: string
      signInButton: string
      signingIn: string
      noAccount: string
      signUp: string
    }
    signUp: {
      title: string
      subtitle: string
      email: string
      emailPlaceholder: string
      phone: string
      phonePlaceholder: string
      password: string
      passwordPlaceholder: string
      confirmPassword: string
      confirmPasswordPlaceholder: string
      language: string
      languageEnglish: string
      languageKhmer: string
      signUpButton: string
      signingUp: string
      hasAccount: string
      signIn: string
      terms: string
      termsLink: string
      privacyLink: string
    }
    forgotPassword: {
      title: string
      subtitle: string
      email: string
      emailPlaceholder: string
      sendButton: string
      sending: string
      backToSignIn: string
      successMessage: string
    }
    resetPassword: {
      title: string
      subtitle: string
      newPassword: string
      newPasswordPlaceholder: string
      confirmPassword: string
      confirmPasswordPlaceholder: string
      resetButton: string
      resetting: string
      successMessage: string
    }
    signOut: {
      title: string
      message: string
      confirmButton: string
      cancelButton: string
    }
  }
  // Common UI elements
  common: {
    loading: string
    error: string
    success: string
    cancel: string
    confirm: string
    save: string
    delete: string
    edit: string
    back: string
    next: string
    previous: string
    submit: string
    close: string
    or: string
    and: string
    comingSoon: string
    active: string
  }
  // Validation messages
  validation: {
    required: string
    invalidEmail: string
    invalidPhone: string
    passwordTooShort: string
    passwordMissingUppercase: string
    passwordMissingLowercase: string
    passwordMissingDigit: string
    passwordMissingSpecial: string
    passwordsDontMatch: string
  }
  // Dashboard
  dashboard: {
    title: string
    welcome: string
    subtitle: string
    totalStudents: string
    classes: string
    attendanceRate: string
    pendingTasks: string
    comingSoon: string
  }
  // Navigation
  nav: {
    // Groups
    overview: string
    management: string
    // Items
    dashboard: string
    students: string
    classes: string
    attendance: string
    schedule: string
    settings: string
    profile: string
    notifications: string
    help: string
    signOut: string
  }
  // Search/Command menu
  search: {
    placeholder: string
    noResults: string
    theme: string
    light: string
    dark: string
    system: string
  }
  // Filter toolbar
  filter: {
    submit: string
    reset: string
    selected: string
    noResults: string
    clearFilters: string
  }
  // Table
  table: {
    view: string
    toggleColumns: string
    rowsSelected: string
    rowsPerPage: string
    page: string
    of: string
    visible: string
    goToFirstPage: string
    goToPreviousPage: string
    goToNextPage: string
    goToLastPage: string
  }
  // Settings
  settings: {
    title: string
    description: string
    profile: {
      title: string
      description: string
      name: string
      namePlaceholder: string
      nameDescription: string
      email: string
      emailPlaceholder: string
      phone: string
      phonePlaceholder: string
      language: string
      languagePlaceholder: string
      languageEnglish: string
      languageKhmer: string
      languageDescription: string
      changePhoto: string
      removePhoto: string
      photoHint: string
      updateButton: string
      updating: string
      updateSuccess: string
      photoUploadSuccess: string
      photoDeleteSuccess: string
      invalidFileType: string
      fileTooLarge: string
    }
    account: {
      title: string
      description: string
      currentPassword: string
      currentPasswordPlaceholder: string
      newPassword: string
      newPasswordPlaceholder: string
      confirmPassword: string
      confirmPasswordPlaceholder: string
      passwordRequirements: string
      changePasswordButton: string
      changingPassword: string
      passwordChangeSuccess: string
    }
    display: {
      title: string
      description: string
      textSize: string
      textSizeDescription: string
      small: string
      medium: string
      large: string
      updateSuccess: string
    }
  }
  // Students
  students: {
    title: string
    description: string
    addStudent: string
    searchPlaceholder: string
    noClass: string
    columns: {
      code: string
      name: string
      fullNameKhmer: string
      gender: string
      dateOfBirth: string
      contact: string
      class: string
      status: string
      actions: string
    }
    actions: {
      view: string
      edit: string
      delete: string
      enroll: string
      transfer: string
    }
    status: {
      ACTIVE: string
      INACTIVE: string
    }
    gender: {
      M: string
      F: string
    }
    enroll: {
      title: string
      class: string
      classPlaceholder: string
      enrollmentDate: string
      enrollmentDatePlaceholder: string
      notes: string
      notesPlaceholder: string
      cancel: string
      submit: string
      enrolling: string
      success: string
    }
    transfer: {
      title: string
      currentClass: string
      noCurrentClass: string
      targetClass: string
      targetClassPlaceholder: string
      transferDate: string
      transferDatePlaceholder: string
      reason: string
      reasonPlaceholder: string
      cancel: string
      submit: string
      transferring: string
      success: string
    }
    view: {
      tabs: {
        information: string
        contacts: string
        enrollment: string
        attendance: string
        grades: string
      }
      personalInfo: string
      contactInfo: string
      enrollmentInfo: string
      noContactInfo: string
      noContacts: string
      noEnrollmentHistory: string
      primaryContact: string
      comingSoon: string
      fields: {
        firstName: string
        lastName: string
        firstNameKhmer: string
        lastNameKhmer: string
        gender: string
        dateOfBirth: string
        age: string
        address: string
        emergencyContact: string
        currentClass: string
        enrollmentDate: string
        endDate: string
        transferDate: string
        status: string
      }
      enrollmentStatus: {
        ACTIVE: string
        COMPLETED: string
        TRANSFERRED: string
        WITHDRAWN: string
      }
      enrollmentReason: {
        NEW: string
        TRANSFER: string
        PROMOTION: string
        REPEAT: string
      }
    }
    modal: {
      addTitle: string
      editTitle: string
      tabs: {
        studentInfo: string
        enrollment: string
        parentContact: string
      }
      fields: {
        firstName: string
        firstNamePlaceholder: string
        lastName: string
        lastNamePlaceholder: string
        firstNameKhmer: string
        firstNameKhmerPlaceholder: string
        lastNameKhmer: string
        lastNameKhmerPlaceholder: string
        dateOfBirth: string
        dateOfBirthPlaceholder: string
        gender: string
        genderPlaceholder: string
        class: string
        classPlaceholder: string
        enrollmentDate: string
        enrollmentDatePlaceholder: string
        address: string
        addressPlaceholder: string
        emergencyContact: string
        emergencyContactPlaceholder: string,
      }
      parentContact: {
        title: string
        addContact: string
        fullName: string
        fullNamePlaceholder: string
        phoneNumber: string
        phoneNumberPlaceholder: string
        relationship: string
        relationshipPlaceholder: string
        isPrimary: string
        contact: string
        relationships: {
          MOTHER: string
          FATHER: string
          GUARDIAN: string
          OTHER: string
        }
        removeContact: string
        atLeastOne: string
      }
      buttons: {
        cancel: string
        save: string
        saving: string
        next: string
        previous: string
      }
      success: {
        created: string
        updated: string
      }
    }
  }
  // Classes
  classes: {
    title: string
    description: string
    addClass: string
    searchPlaceholder: string
    columns: {
      className: string
      grade: string
      academicYear: string
      teacher: string
      enrollment: string
      level: string
      type: string
      status: string
      actions: string
    }
    actions: {
      view: string
      edit: string
      delete: string
      manageStudents: string
    }
    status: {
      ACTIVE: string
      INACTIVE: string
      COMPLETED: string
    }
    level: {
      PRIMARY: string
      SECONDARY: string
      HIGH_SCHOOL: string
    }
    type: {
      NORMAL: string
      SCIENCE: string
      SOCIAL_SCIENCE: string
    }
    modal: {
      addTitle: string
      editTitle: string
      fields: {
        name: string
        namePlaceholder: string
        description: string
        descriptionPlaceholder: string
        academicYear: string
        academicYearPlaceholder: string
        grade: string
        gradePlaceholder: string
        section: string
        sectionPlaceholder: string
        capacity: string
        capacityPlaceholder: string
        level: string
        levelPlaceholder: string
        type: string
        typePlaceholder: string
        status: string
        statusPlaceholder: string
      }
      buttons: {
        cancel: string
        save: string
        saving: string
      }
      success: {
        created: string
        updated: string
      }
    }
    tabs?: {
      ariaLabel: string
      students: string
      schedule: string
      attendance: string
      grades: string
    }
    detail?: {
      searchPlaceholder: string
      searchAriaLabel: string
      noResults: string
      statusFilterLabel: string
      statusFilterPlaceholder: string
      statusOptions?: {
        ALL: string
      }
    }
    transfer?: {
      transferButton: string
      selectStudents: string
      selectedCount: string
      dialog: {
        title: string
        description: string
        selectedStudents: string
        studentsSelected: string
        destinationClass: string
        destinationClassPlaceholder: string
        noEligibleClasses: string
        warning: string
        confirmButton: string
        transferring: string
        successMessage: string
        partialSuccessMessage: string
        undoHint: string
        undoButton: string
        undoSuccess: string
      }
    }
  }
  // School Setup
  schoolSetup: {
    title: string
    subtitle: string
    step1: {
      title: string
      province: string
      provincePlaceholder: string
      district: string
      districtPlaceholder: string
      districtDisabled: string
      schools: string
      schoolsCount: string
      selectProvince: string
      loadingProvinces: string
      loadingDistricts: string
      loadingSchools: string
      noDistricts: string
      noSchools: string
      addNewSchool: string
      continueButton: string
      selectSchoolError: string
    }
    step2: {
      title: string
      backToSelection: string
      principalName: string
      principalNamePlaceholder: string
      principalGender: string
      principalGenderPlaceholder: string
      genderMale: string
      genderFemale: string
      backButton: string
      completeButton: string
      updateButton: string
      completing: string
    }
    currentSetup: {
      description: string
      editButton: string
    }
    table: {
      schoolName: string
      type: string
      address: string
      action: string
      selectButton: string
      selectedButton: string
    }
    types: {
      PRIMARY: string
      SECONDARY: string
      HIGH_SCHOOL: string
      VOCATIONAL: string
    }
    errors: {
      failedToLoad: string
      setupFailed: string
    }
    success: {
      setupComplete: string
    }
    warning?: {
      title: string
      description: string
    }
    addSchool?: {
      title: string
      description: string
      name: string
      namePlaceholder: string
      nameKhmer: string
      nameKhmerPlaceholder: string
      type: string
      typePlaceholder: string
      address: string
      addressPlaceholder: string
      createButton: string
      creating: string
      success: string
      error: string
    }
  }
}

/**
 * Complete translations structure
 */
export interface Translations {
  errors: ErrorCodeTranslations
  ui: UITranslations
}

/**
 * All translations keyed by language
 */
export type TranslationsByLanguage = Record<Language, Translations>
