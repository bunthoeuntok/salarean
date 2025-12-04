/**
 * Transfer-related TypeScript types for batch student transfer feature
 */

/**
 * Request type for batch student transfer
 */
export interface BatchTransferRequest {
  destinationClassId: string;
  studentIds: string[];
}

/**
 * Response type for batch student transfer
 */
export interface BatchTransferResponse {
  transferId: string;
  sourceClassId: string;
  destinationClassId: string;
  successfulTransfers: number;
  failedTransfers: FailedTransfer[];
  transferredAt: string; // ISO 8601 timestamp
}

/**
 * Details of a student that failed to transfer
 */
export interface FailedTransfer {
  studentId: string;
  studentName: string;
  reason: string; // Error code (e.g., "ALREADY_ENROLLED")
}

/**
 * Response type for undo transfer operation
 */
export interface UndoTransferResponse {
  transferId: string;
  undoneStudents: number;
  sourceClassId: string;
  undoneAt: string; // ISO 8601 timestamp
}

/**
 * Response type for eligible destination classes
 */
export interface EligibleClassResponse {
  id: string;
  name: string;
  code: string;
  gradeLevel: number;
  capacity: number;
  currentEnrollment: number;
  teacherName: string;
}

/**
 * Undo state stored in session storage (Zustand store)
 */
export interface UndoState {
  transferId: string;
  sourceClassId: string;
  sourceClassName: string;
  destinationClassId: string;
  destinationClassName: string;
  studentIds: string[];
  studentNames: string[]; // For display in toast
  performedBy: string; // Current user ID
  transferredAt: number; // Unix timestamp (ms)
  expiresAt: number; // transferredAt + 5 minutes (300000 ms)
}

/**
 * Zustand store interface for undo state management
 */
export interface UndoStore {
  undoState: UndoState | null;
  setUndoState: (state: UndoState) => void;
  clearUndoState: () => void;
  isUndoAvailable: () => boolean;
}

/**
 * Student row type for DataTable with selection
 */
export interface StudentRow {
  id: string;
  studentCode: string;
  name: string;
  enrollmentDate: string;
  enrollmentStatus: 'ACTIVE' | 'TRANSFERRED' | 'WITHDRAWN';
  profilePhotoUrl?: string;
}

/**
 * Transfer dialog state
 */
export interface TransferDialogState {
  open: boolean;
  selectedStudents: StudentRow[];
  destinationClassId: string | null;
  isTransferring: boolean;
  error: string | null;
}
