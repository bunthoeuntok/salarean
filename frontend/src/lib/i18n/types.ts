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
  }
  // Table
  table: {
    view: string
    toggleColumns: string
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
  }
  // Students
  students: {
    title: string
    description: string
    addStudent: string
    searchPlaceholder: string
    columns: {
      code: string
      name: string
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
      name: string
      grade: string
      academicYear: string
      teacher: string
      enrollment: string
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
