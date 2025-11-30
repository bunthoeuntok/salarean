import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { format } from 'date-fns'
import {
  User,
  Users,
  History,
  Calendar,
  GraduationCap,
  Phone,
  MapPin,
  Loader2,
  AlertCircle,
} from 'lucide-react'

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { cn } from '@/lib/utils'
import { useLanguage } from '@/context/language-provider'
import { studentService } from '@/services/student.service'
import type { Student, EnrollmentResponse } from '@/types/student.types'

interface ViewStudentModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  student: Student | null
}

type TabValue = 'information' | 'contacts' | 'enrollment' | 'attendance' | 'grades'

export function ViewStudentModal({ open, onOpenChange, student }: ViewStudentModalProps) {
  const { t } = useLanguage()
  const [activeTab, setActiveTab] = useState<TabValue>('information')

  // Fetch full student details when modal opens (for contacts)
  const { data: studentDetails, isLoading: isLoadingStudent } = useQuery({
    queryKey: ['student', student?.id],
    queryFn: () => studentService.getStudent(student!.id),
    enabled: open && !!student?.id && activeTab === 'contacts',
    staleTime: 5 * 60 * 1000,
  })

  // Fetch enrollment history when tab is active
  const { data: enrollmentHistory, isLoading: isLoadingHistory } = useQuery({
    queryKey: ['student-enrollment-history', student?.id],
    queryFn: () => studentService.getEnrollmentHistory(student!.id),
    enabled: open && !!student?.id && activeTab === 'enrollment',
    staleTime: 5 * 60 * 1000,
  })

  const handleClose = () => {
    onOpenChange(false)
    setActiveTab('information')
  }

  if (!student) return null

  const initials = `${student.firstName[0]}${student.lastName[0]}`

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className='h-[90vh] w-full flex flex-col p-0'>
        {/* Header */}
        <DialogHeader className='shrink-0 bg-muted/50 border-b px-6 py-4'>
          <div className='flex items-center justify-between'>
            <div className='flex items-center gap-4'>
              <Avatar className='h-16 w-16'>
                <AvatarImage src={student.photoUrl} alt={student.fullName} />
                <AvatarFallback className='text-lg'>{initials}</AvatarFallback>
              </Avatar>
              <div>
                <DialogTitle className='text-xl'>
                  {student.fullNameKhmer || student.fullName}
                </DialogTitle>
                {student.fullNameKhmer && student.fullName && (
                  <p className='text-sm text-muted-foreground'>{student.fullName}</p>
                )}
                <div className='flex items-center gap-2 mt-1'>
                  <Badge variant='outline'>{student.studentCode}</Badge>
                  <Badge variant={student.status === 'ACTIVE' ? 'default' : 'secondary'}>
                    {t.students.status[student.status]}
                  </Badge>
                  {student.currentClassName && (
                    <Badge variant='outline'>{student.currentClassName}</Badge>
                  )}
                </div>
              </div>
            </div>
          </div>
        </DialogHeader>

        {/* Tabs */}
        <Tabs
          value={activeTab}
          onValueChange={(v) => setActiveTab(v as TabValue)}
          className='flex-1 flex flex-col overflow-hidden'
        >
          <TabsList className='shrink-0 w-full justify-start rounded-none border-b bg-transparent px-6 h-12'>
            <TabsTrigger value='information' className='gap-2'>
              <User className='h-4 w-4' />
              {t.students.view.tabs.information}
            </TabsTrigger>
            <TabsTrigger value='contacts' className='gap-2'>
              <Users className='h-4 w-4' />
              {t.students.view.tabs.contacts}
            </TabsTrigger>
            <TabsTrigger value='enrollment' className='gap-2'>
              <History className='h-4 w-4' />
              {t.students.view.tabs.enrollment}
            </TabsTrigger>
            <TabsTrigger value='attendance' className='gap-2' disabled>
              <Calendar className='h-4 w-4' />
              {t.students.view.tabs.attendance}
            </TabsTrigger>
            <TabsTrigger value='grades' className='gap-2' disabled>
              <GraduationCap className='h-4 w-4' />
              {t.students.view.tabs.grades}
            </TabsTrigger>
          </TabsList>

          <div className='flex-1 overflow-auto p-6'>
            {/* Information Tab */}
            <TabsContent value='information' className='mt-0 h-full'>
              <InformationTab student={student} t={t} />
            </TabsContent>

            {/* Contacts Tab */}
            <TabsContent value='contacts' className='mt-0 h-full'>
              <ContactsTab
                student={studentDetails || student}
                isLoading={isLoadingStudent}
                t={t}
              />
            </TabsContent>

            {/* Enrollment History Tab */}
            <TabsContent value='enrollment' className='mt-0 h-full'>
              <EnrollmentHistoryTab
                enrollments={enrollmentHistory?.enrollments}
                isLoading={isLoadingHistory}
                t={t}
              />
            </TabsContent>

            {/* Attendance Tab (Coming Soon) */}
            <TabsContent value='attendance' className='mt-0 h-full'>
              <ComingSoonTab t={t} />
            </TabsContent>

            {/* Grades Tab (Coming Soon) */}
            <TabsContent value='grades' className='mt-0 h-full'>
              <ComingSoonTab t={t} />
            </TabsContent>
          </div>
        </Tabs>
      </DialogContent>
    </Dialog>
  )
}

// Information Tab Component
function InformationTab({
  student,
  t,
}: {
  student: Student
  t: ReturnType<typeof useLanguage>['t']
}) {
  return (
    <div className='grid gap-6 md:grid-cols-2'>
      <Card>
        <CardHeader>
          <CardTitle className='text-base'>{t.students.view.personalInfo}</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <InfoRow label={t.students.view.fields.firstName} value={student.firstName} />
          <InfoRow label={t.students.view.fields.lastName} value={student.lastName} />
          {student.firstNameKhmer && (
            <InfoRow label={t.students.view.fields.firstNameKhmer} value={student.firstNameKhmer} />
          )}
          {student.lastNameKhmer && (
            <InfoRow label={t.students.view.fields.lastNameKhmer} value={student.lastNameKhmer} />
          )}
          <InfoRow
            label={t.students.view.fields.gender}
            value={t.students.gender[student.gender]}
          />
          <InfoRow
            label={t.students.view.fields.dateOfBirth}
            value={student.dateOfBirth ? format(new Date(student.dateOfBirth), 'dd/MM/yyyy') : '-'}
          />
          {student.age !== undefined && (
            <InfoRow label={t.students.view.fields.age} value={`${student.age}`} />
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className='text-base'>{t.students.view.contactInfo}</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          {student.address && (
            <div className='flex items-start gap-2'>
              <MapPin className='h-4 w-4 mt-0.5 text-muted-foreground' />
              <div>
                <p className='text-sm text-muted-foreground'>{t.students.view.fields.address}</p>
                <p className='text-sm'>{student.address}</p>
              </div>
            </div>
          )}
          {student.emergencyContact && (
            <div className='flex items-start gap-2'>
              <Phone className='h-4 w-4 mt-0.5 text-muted-foreground' />
              <div>
                <p className='text-sm text-muted-foreground'>{t.students.view.fields.emergencyContact}</p>
                <p className='text-sm'>{student.emergencyContact}</p>
              </div>
            </div>
          )}
          {!student.address && !student.emergencyContact && (
            <p className='text-sm text-muted-foreground'>{t.students.view.noContactInfo}</p>
          )}
        </CardContent>
      </Card>

      <Card className='md:col-span-2'>
        <CardHeader>
          <CardTitle className='text-base'>{t.students.view.enrollmentInfo}</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='grid gap-4 md:grid-cols-3'>
            <InfoRow
              label={t.students.view.fields.currentClass}
              value={student.currentClassName || '-'}
            />
            <InfoRow
              label={t.students.view.fields.enrollmentDate}
              value={student.enrollmentDate ? format(new Date(student.enrollmentDate), 'dd/MM/yyyy') : '-'}
            />
            <InfoRow
              label={t.students.view.fields.status}
              value={t.students.status[student.status]}
            />
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

// Contacts Tab Component
function ContactsTab({
  student,
  isLoading,
  t,
}: {
  student: Student
  isLoading: boolean
  t: ReturnType<typeof useLanguage>['t']
}) {
  if (isLoading) {
    return (
      <div className='flex items-center justify-center h-64'>
        <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
      </div>
    )
  }

  const contacts = student.parentContacts || []

  if (contacts.length === 0) {
    return (
      <div className='flex flex-col items-center justify-center h-64 text-muted-foreground'>
        <Users className='h-12 w-12 mb-4' />
        <p>{t.students.view.noContacts}</p>
      </div>
    )
  }

  return (
    <div className='grid gap-4 md:grid-cols-2'>
      {contacts.map((contact, index) => (
        <Card key={contact.id || index}>
          <CardContent className='pt-6'>
            <div className='flex items-start justify-between'>
              <div className='flex items-center gap-3'>
                <div className='h-10 w-10 rounded-full bg-muted flex items-center justify-center'>
                  <User className='h-5 w-5 text-muted-foreground' />
                </div>
                <div>
                  <p className='font-medium'>{contact.fullName}</p>
                  <p className='text-sm text-muted-foreground'>
                    {t.students.modal.parentContact.relationships[contact.relationship]}
                  </p>
                </div>
              </div>
              {contact.isPrimary && (
                <Badge variant='default'>{t.students.view.primaryContact}</Badge>
              )}
            </div>
            <Separator className='my-4' />
            <div className='flex items-center gap-2'>
              <Phone className='h-4 w-4 text-muted-foreground' />
              <span className='text-sm'>{contact.phoneNumber}</span>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}

// Enrollment History Tab Component
function EnrollmentHistoryTab({
  enrollments,
  isLoading,
  t,
}: {
  enrollments?: EnrollmentResponse[]
  isLoading: boolean
  t: ReturnType<typeof useLanguage>['t']
}) {
  if (isLoading) {
    return (
      <div className='flex items-center justify-center h-64'>
        <Loader2 className='h-8 w-8 animate-spin text-muted-foreground' />
      </div>
    )
  }

  if (!enrollments || enrollments.length === 0) {
    return (
      <div className='flex flex-col items-center justify-center h-64 text-muted-foreground'>
        <History className='h-12 w-12 mb-4' />
        <p>{t.students.view.noEnrollmentHistory}</p>
      </div>
    )
  }

  const statusColors: Record<string, string> = {
    ACTIVE: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    COMPLETED: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    TRANSFERRED: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    WITHDRAWN: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
  }

  return (
    <div className='space-y-4'>
      {enrollments.map((enrollment) => (
        <Card key={enrollment.id}>
          <CardContent className='pt-6'>
            <div className='flex items-start justify-between'>
              <div>
                <div className='flex items-center gap-2'>
                  <p className='font-medium'>{enrollment.className}</p>
                  <Badge className={cn('font-normal', statusColors[enrollment.status])}>
                    {t.students.view.enrollmentStatus[enrollment.status]}
                  </Badge>
                </div>
                <p className='text-sm text-muted-foreground mt-1'>{enrollment.schoolName}</p>
              </div>
              <Badge variant='outline'>
                {t.students.view.enrollmentReason[enrollment.reason]}
              </Badge>
            </div>
            <Separator className='my-4' />
            <div className='grid gap-2 text-sm md:grid-cols-3'>
              <div>
                <span className='text-muted-foreground'>{t.students.view.fields.enrollmentDate}: </span>
                <span>{format(new Date(enrollment.enrollmentDate), 'dd/MM/yyyy')}</span>
              </div>
              {enrollment.endDate && (
                <div>
                  <span className='text-muted-foreground'>{t.students.view.fields.endDate}: </span>
                  <span>{format(new Date(enrollment.endDate), 'dd/MM/yyyy')}</span>
                </div>
              )}
              {enrollment.transferDate && (
                <div>
                  <span className='text-muted-foreground'>{t.students.view.fields.transferDate}: </span>
                  <span>{format(new Date(enrollment.transferDate), 'dd/MM/yyyy')}</span>
                </div>
              )}
            </div>
            {(enrollment.notes || enrollment.transferReason) && (
              <>
                <Separator className='my-4' />
                <p className='text-sm text-muted-foreground'>
                  {enrollment.transferReason || enrollment.notes}
                </p>
              </>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}

// Coming Soon Tab Component
function ComingSoonTab({ t }: { t: ReturnType<typeof useLanguage>['t'] }) {
  return (
    <div className='flex flex-col items-center justify-center h-64 text-muted-foreground'>
      <AlertCircle className='h-12 w-12 mb-4' />
      <p>{t.students.view.comingSoon}</p>
    </div>
  )
}

// Helper Component
function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className='text-sm text-muted-foreground'>{label}</p>
      <p className='text-sm font-medium'>{value}</p>
    </div>
  )
}
