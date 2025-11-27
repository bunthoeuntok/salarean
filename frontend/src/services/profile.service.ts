import api, { apiRequest } from '@/lib/api'
import type {
  AuthUser,
  UpdateProfileRequest,
  ChangePasswordRequest,
  PhotoUploadResponse,
} from '@/types/auth.types'

/**
 * Profile service for profile-related API calls
 */
export const profileService = {
  /**
   * Update user profile
   * @param data - Profile data to update (name, email, phoneNumber, preferredLanguage)
   * @returns Promise with updated user data
   */
  async updateProfile(data: UpdateProfileRequest): Promise<AuthUser> {
    return apiRequest<AuthUser>(api.put('/api/profile/me', data))
  },

  /**
   * Change user password
   * @param data - Current and new password
   * @returns Promise that resolves on successful password change
   */
  async changePassword(data: ChangePasswordRequest): Promise<void> {
    await apiRequest<void>(api.put('/api/profile/password', data))
  },

  /**
   * Upload profile photo
   * @param file - Image file to upload
   * @returns Promise with photo URL and upload timestamp
   */
  async uploadPhoto(file: File): Promise<PhotoUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    return apiRequest<PhotoUploadResponse>(
      api.post('/api/profile/photo', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
    )
  },

  /**
   * Delete profile photo
   * @returns Promise that resolves on successful deletion
   */
  async deletePhoto(): Promise<void> {
    await apiRequest<void>(api.delete('/api/profile/photo'))
  },
}
