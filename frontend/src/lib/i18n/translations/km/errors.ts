import type { ErrorCodeTranslations } from '../../types'

export const errorsKm: ErrorCodeTranslations = {
  // Success
  SUCCESS: 'ប្រតិបត្តិការបានជោគជ័យ',

  // Common validation errors
  VALIDATION_ERROR: 'កំហុសក្នុងការផ្ទៀងផ្ទាត់',
  INVALID_INPUT: 'ទិន្នន័យមិនត្រឹមត្រូវ',
  REQUIRED_FIELD_MISSING: 'ត្រូវការបំពេញព័ត៌មាន',
  INVALID_DATE_FORMAT: 'ទម្រង់កាលបរិច្ឆេទមិនត្រឹមត្រូវ',
  INVALID_PHONE_FORMAT: 'លេខទូរសព្ទមិនត្រឹមត្រូវ',
  INVALID_EMAIL_FORMAT: 'អ៊ីមែលមិនត្រឹមត្រូវ',

  // Authentication & Authorization
  UNAUTHORIZED: 'សូមចូលគណនីដើម្បីបន្ត',
  FORBIDDEN: 'អ្នកមិនមានសិទ្ធិចូលប្រើប្រាស់ធនធាននេះទេ',
  INVALID_TOKEN: 'សម័យមិនត្រឹមត្រូវ។ សូមចូលគណនីម្តងទៀត',
  TOKEN_EXPIRED: 'សម័យផុតកំណត់។ សូមចូលគណនីម្តងទៀត',
  TOKEN_REPLAY_DETECTED: 'រកឃើញបញ្ហាសុវត្ថិភាព។ សូមចូលគណនីម្តងទៀត',
  SESSION_EXPIRED: 'សម័យរបស់អ្នកបានផុតកំណត់។ សូមចូលគណនីម្តងទៀត',

  // Resource errors
  RESOURCE_NOT_FOUND: 'រកមិនឃើញធនធាន',
  RESOURCE_ALREADY_EXISTS: 'ធនធានមានរួចហើយ',
  RESOURCE_DELETED: 'ធនធាននេះត្រូវបានលុប',

  // File upload errors
  FILE_SIZE_EXCEEDED: 'ទំហំឯកសារលើសពីកំណត់អតិបរមា',
  INVALID_FILE_FORMAT: 'ទម្រង់ឯកសារមិនត្រឹមត្រូវ',
  FILE_UPLOAD_FAILED: 'ការផ្ទុកឡើងឯកសារបរាជ័យ',
  CORRUPTED_FILE: 'ឯកសារខូច ឬមិនអាចអានបាន',

  // System errors
  INTERNAL_ERROR: 'មានកំហុសកើតឡើង។ សូមព្យាយាមម្តងទៀត',
  SERVICE_UNAVAILABLE: 'សេវាកម្មមិនអាចប្រើបានបណ្តោះអាសន្ន',
  RATE_LIMIT_EXCEEDED: 'សំណើច្រើនពេក។ សូមរង់ចាំហើយព្យាយាមម្តងទៀត',
  DATABASE_ERROR: 'មានកំហុសមូលដ្ឋានទិន្នន័យ',

  // Frontend-specific errors
  NETWORK_ERROR: 'កំហុសបណ្តាញ។ សូមពិនិត្យការភ្ជាប់របស់អ្នកហើយព្យាយាមម្តងទៀត',

  // Auth-specific errors
  INVALID_CREDENTIALS: 'អ៊ីមែល/ទូរសព្ទ ឬពាក្យសម្ងាត់មិនត្រឹមត្រូវ',
  ACCOUNT_LOCKED: 'គណនីត្រូវបានចាក់សោដោយសារការព្យាយាមច្រើនដងពេក',
  DUPLICATE_EMAIL: 'អ៊ីមែលនេះបានចុះឈ្មោះរួចហើយ',
  DUPLICATE_PHONE: 'លេខទូរសព្ទនេះបានចុះឈ្មោះរួចហើយ',
  INVALID_PASSWORD: 'ពាក្យសម្ងាត់មិនត្រឹមត្រូវ',
  WEAK_PASSWORD: 'ពាក្យសម្ងាត់មិនបំពេញតម្រូវការ',
  PASSWORD_TOO_SHORT: 'ពាក្យសម្ងាត់ត្រូវមានយ៉ាងតិច ៨ តួអក្សរ',
  PASSWORD_MISSING_UPPERCASE: 'ពាក្យសម្ងាត់ត្រូវមានអក្សរធំ',
  PASSWORD_MISSING_LOWERCASE: 'ពាក្យសម្ងាត់ត្រូវមានអក្សរតូច',
  PASSWORD_MISSING_DIGIT: 'ពាក្យសម្ងាត់ត្រូវមានលេខ',
  PASSWORD_MISSING_SPECIAL: 'ពាក្យសម្ងាត់ត្រូវមានតួអក្សរពិសេស',
  PASSWORD_TOO_COMMON: 'ពាក្យសម្ងាត់នេះសាមញ្ញពេក',
  USER_NOT_FOUND: 'រកមិនឃើញអ្នកប្រើប្រាស់',
  EMAIL_NOT_FOUND: 'រកមិនឃើញអ៊ីមែល',
  RESET_TOKEN_INVALID: 'តំណកំណត់ពាក្យសម្ងាត់ឡើងវិញមិនត្រឹមត្រូវ',
  RESET_TOKEN_EXPIRED: 'តំណកំណត់ពាក្យសម្ងាត់ឡើងវិញបានផុតកំណត់',
  PROFILE_UPDATE_FAILED: 'បរាជ័យក្នុងការធ្វើបច្ចុប្បន្នភាពព័ត៌មាន',
  INVALID_LANGUAGE: 'ការជ្រើសរើសភាសាមិនត្រឹមត្រូវ',
  PHOTO_SIZE_EXCEEDED: 'ទំហំរូបភាពលើសពី 5MB',
  INVALID_PHOTO_FORMAT: 'រូបភាពត្រូវជា JPEG, PNG, ឬ WebP',
  PHOTO_UPLOAD_FAILED: 'បរាជ័យក្នុងការផ្ទុកឡើងរូបភាព',
  CORRUPTED_IMAGE: 'ឯកសាររូបភាពខូច',

  // Student transfer errors
  SOURCE_CLASS_NOT_FOUND: 'រកមិនឃើញថ្នាក់ប្រភព',
  DESTINATION_CLASS_NOT_FOUND: 'រកមិនឃើញថ្នាក់ទិសដៅ',
  SOURCE_CLASS_NOT_ACTIVE: 'ថ្នាក់ប្រភពមិនសកម្ម',
  DESTINATION_CLASS_NOT_ACTIVE: 'ថ្នាក់ទិសដៅមិនសកម្ម',
  GRADE_MISMATCH: 'មិនអាចផ្ទេរសិស្សរវាងកម្រិតផ្សេងគ្នាបានទេ',
  INSUFFICIENT_CAPACITY: 'ថ្នាក់ទិសដៅគ្មានសមត្ថភាពគ្រប់គ្រាន់',
  STUDENT_NOT_FOUND: 'រកមិនឃើញសិស្ស',
  STUDENT_NOT_ENROLLED_IN_SOURCE: 'សិស្សមិនបានចុះឈ្មោះក្នុងថ្នាក់ប្រភព',
  ALREADY_ENROLLED_IN_DESTINATION: 'សិស្សបានចុះឈ្មោះក្នុងថ្នាក់ទិសដៅរួចហើយ',

  // Undo transfer errors
  TRANSFER_NOT_FOUND: 'រកមិនឃើញកំណត់ត្រាផ្ទេរ',
  TRANSFER_ALREADY_UNDONE: 'ការផ្ទេរនេះត្រូវបានលុបចោលរួចហើយ',
  UNAUTHORIZED_UNDO: 'អ្នកមិនមានសិទ្ធិលុបចោលការផ្ទេរនេះទេ',
  UNDO_WINDOW_EXPIRED: 'មិនអាចលុបចោលបានទេ - រយៈពេល ៥ នាទីបានផុតកំណត់',
  INVALID_METADATA: 'ទិន្នន័យផ្ទេរមិនត្រឹមត្រូវ',
}
