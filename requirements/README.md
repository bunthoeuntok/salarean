# Student Management System - Documentation

**Generated:** November 19, 2025  
**Version:** 2.0 (Updated with Monthly Exam System)

---

## 📋 Documents

### 1. [SMS_PRD_Executive_Summary.md](SMS_PRD_Executive_Summary.md)
**Type:** Product Requirements Document  
**Pages:** ~12  
**Audience:** Stakeholders, investors, management

**Key Sections:**
- Executive Summary & Market Opportunity
- Core Product Features (with monthly exam system)
- User Personas
- Business Model & Pricing
- 12-Month Roadmap

### 2. [SMS_Technical_Architecture.md](SMS_Technical_Architecture.md)
**Type:** Technical Architecture  
**Pages:** ~55  
**Audience:** Development team, architects

**Key Sections:**
- Complete Docker infrastructure
- 7 Microservices architecture
- **Student enrollment history system** ✨
- **Monthly exam & grading system** ✨
- Complete database schemas
- API specifications

---

## 🎯 Key Features

### 1. Student Enrollment History (Option 3)
Tracks complete history of class changes:

```sql
-- Dedicated table for enrollment tracking
CREATE TABLE student_class_enrollments (
    student_id UUID,
    class_id UUID,
    enrollment_date DATE,
    end_date DATE,  -- NULL = currently enrolled
    reason VARCHAR  -- NEW, TRANSFER, PROMOTION
);
```

**Benefits:**
- ✅ Complete audit trail
- ✅ Historical accuracy (know class on any date)
- ✅ No data loss when students transfer
- ✅ Accurate reporting across transfers

### 2. Grading System (Monthly + Semester)

**Structure per Semester:**
- **4 Monthly Exams** (customizable: 2-6)
- **1 Semester Exam**

**Calculation:**
```
Monthly Average = (Exam1 + Exam2 + Exam3 + Exam4) / 4
Semester Average = (Monthly Average + Semester Exam) / 2
Annual Average = (Semester1 + Semester2) / 2
```

**Reports:**
1. **Monthly Progress Reports** (10 per year)
   - Track ongoing performance
   - One month's scores across all subjects

2. **Semester Report Cards** (2 per year)
   - All monthly exam scores
   - Semester exam score
   - Calculated averages
   - Class rank

3. **Annual Transcripts** (1 per year)
   - Complete year summary
   - Both semester averages
   - Final rank
   - Promotion status

**Teacher Customization:**
- Teachers can adjust number of monthly exams (2-6 per semester)
- Default is 4 monthly exams (MoEYS standard)
- Simple interface in settings

---

## 📊 Complete Academic Year Structure

```
Academic Year 2024-2025 (10 months)
│
├── Semester 1 (Nov - Mar)
│   ├── Monthly Exam 1 (Nov)
│   ├── Monthly Exam 2 (Dec)
│   ├── Monthly Exam 3 (Jan)
│   ├── Monthly Exam 4 (Feb)
│   └── Semester Exam (Mar)
│
└── Semester 2 (Apr - Aug)
    ├── Monthly Exam 1 (Apr)
    ├── Monthly Exam 2 (May)
    ├── Monthly Exam 3 (Jun)
    ├── Monthly Exam 4 (Jul)
    └── Semester Exam (Aug)
```

---

## 🔧 Technical Stack

**Frontend:**
- Next.js 14 (responsive web app)
- React, TypeScript, TailwindCSS
- Server-side rendering
- Optimized for 3G/4G

**Backend:**
- Spring Boot 3 microservices
- PostgreSQL (6 databases)
- Redis (caching)
- RabbitMQ (messaging)

**Infrastructure:**
- Docker & Docker Compose
- Nginx reverse proxy
- Cloud hosting (AWS/GCP/DigitalOcean)

---

## 📐 Database Design Highlights

### Grade Database (grade_db)

**Main Tables:**

1. **assessment_types** - Monthly and semester exam templates
2. **teacher_assessment_config** - Teacher customization (2-6 monthly exams)
3. **grades** - Individual exam scores
4. **grade_averages** - Cached calculations (performance)

**Example Grade Entry:**
```sql
-- Student takes Math monthly exam 1
INSERT INTO grades VALUES (
    student_id: 'student-123',
    subject_id: 'math',
    assessment_type: 'monthly_exam',
    year: '2024-2025',
    semester: 'SEMESTER_1',
    month: 1,
    score: 75,
    max_score: 100
);
```

**Example Calculation:**
```sql
-- Calculate semester average
SELECT calculate_subject_average(
    'student-123',
    'math',
    '2024-2025',
    'SEMESTER_1'
);

-- Returns:
-- monthly_avg: 77.5
-- semester_exam: 82.0
-- overall_avg: 79.75
-- letter_grade: B
```

---

## 🚀 API Examples

### Enter Monthly Exam Grades (Bulk)
```http
POST /api/grades/monthly/{classId}/{subjectId}
Content-Type: application/json

{
  "month": 1,
  "semester": "SEMESTER_1",
  "year": "2024-2025",
  "grades": [
    { "studentId": "student-1", "score": 75 },
    { "studentId": "student-2", "score": 82 },
    { "studentId": "student-3", "score": 68 }
  ]
}
```

### Generate Semester Report Card
```http
POST /api/reports/semester/{studentId}/semester/SEMESTER_1
Accept: application/pdf

Returns: PDF report with all grades, averages, rank
```

### Get Student Grades for Semester
```http
GET /api/grades/student/{studentId}/semester/SEMESTER_1

Response:
{
  "studentId": "student-123",
  "semester": "SEMESTER_1",
  "subjects": [
    {
      "subjectId": "math",
      "monthlyExams": [75, 80, 70, 85],
      "monthlyAverage": 77.5,
      "semesterExam": 82,
      "overallAverage": 79.75,
      "letterGrade": "B",
      "rank": 12
    },
    ...
  ]
}
```

### Transfer Student to New Class
```http
POST /api/students/{studentId}/transfer
Content-Type: application/json

{
  "newClassId": "class-5b-uuid",
  "transferDate": "2025-03-01",
  "reason": "TRANSFER",
  "notes": "Class size balancing"
}
```

---

## 📝 Letter Grade Scale (MoEYS)

| Grade | Percentage | Khmer | Description |
|-------|-----------|-------|-------------|
| **A** | 85-100% | ល្អបំផុត | Excellent |
| **B** | 70-84% | ល្អ | Very Good |
| **C** | 55-69% | មធ្យម | Good |
| **D** | 40-54% | ខ្សោយ | Fair |
| **E** | 25-39% | ខ្សោយណាស់ | Poor |
| **F** | 0-24% | ធ្លាក់ | Fail |

---

## ✨ What's New in Version 2.0

### ✅ Added:
1. **Monthly exam system** (4 exams per semester, customizable)
2. **Three report types** (monthly, semester, annual)
3. **Teacher grading configuration** (customize exam frequency)
4. **Enhanced grade calculation** (simple averaging)
5. **Complete grading system documentation**

### ✅ Updated:
1. Grade database schema
2. Grade Service API endpoints
3. Report Service capabilities
4. Calculation logic and examples
5. PRD feature descriptions

---

## 🎯 Quick Start

### For Business/Product Team
1. Read: **SMS_PRD_Executive_Summary.md**
2. Focus on: Grading system, reports, user flows
3. Review: Business model and roadmap

### For Development Team
1. Read: **SMS_Technical_Architecture.md**
2. Study: Section 6.4 (Grade DB Schema)
3. Study: Section 11 (Grading System Summary)
4. Review: API endpoints for grades and reports
5. Follow: Docker deployment instructions

---

## 📞 Implementation Checklist

### Week 1-2: Database Setup
- [ ] Create grade_db schema
- [ ] Insert default assessment types
- [ ] Create utility functions
- [ ] Test grade calculations

### Week 3-4: Grade Service
- [ ] Implement grade entry endpoints
- [ ] Build calculation logic
- [ ] Add teacher configuration endpoints
- [ ] Test with sample data

### Week 5-6: Report Service
- [ ] Design report templates (PDF)
- [ ] Implement monthly report generation
- [ ] Implement semester report generation
- [ ] Implement annual transcript generation

### Week 7-8: Frontend
- [ ] Grade entry UI (monthly exams)
- [ ] Grade entry UI (semester exams)
- [ ] Settings: Configure monthly exams count
- [ ] Report generation interface

### Week 9-10: Testing & Polish
- [ ] End-to-end testing
- [ ] Performance testing (bulk operations)
- [ ] UI/UX refinement
- [ ] Documentation

### Week 11-12: MVP Launch
- [ ] Deploy to staging
- [ ] User acceptance testing with 5-10 teachers
- [ ] Fix bugs
- [ ] Production deployment

---

## 💡 Key Design Decisions

### Why Simple Averaging?
✅ Easy for teachers to understand  
✅ Transparent to students/parents  
✅ Fast calculations  
✅ MoEYS compatible  

### Why Configurable Monthly Exams?
✅ Different schools have different schedules  
✅ Some teachers prefer more/fewer assessments  
✅ Flexibility without complexity  
✅ Default (4) covers most cases  

### Why Three Report Types?
✅ **Monthly:** Quick progress tracking  
✅ **Semester:** Comprehensive evaluation  
✅ **Annual:** Official transcript for records  

---

## 📊 Performance Targets

| Operation | Target | Notes |
|-----------|--------|-------|
| Enter grades (single student) | <200ms | Including validation |
| Enter grades (bulk, 35 students) | <2s | Typical class size |
| Calculate semester average | <100ms | Per student |
| Generate monthly report | <10s | Single student |
| Generate semester report | <30s | Single student |
| Generate bulk reports (class) | <5min | 35 students |

---

## 🔒 Data Integrity Rules

1. **Grade Entry:**
   - Score cannot exceed max_score
   - Each assessment can only be entered once per student
   - Past grades can be edited (with audit log)

2. **Report Generation:**
   - Reports are immutable once generated
   - Stored with generation timestamp
   - Can be regenerated if grades change

3. **Enrollment Changes:**
   - Complete history maintained
   - Grades stay with historical class
   - No data loss on transfer

---

## 📚 Additional Resources

**MoEYS Curriculum:**
- Grade levels: 1-12 (6+3+3 structure)
- Primary: Grades 1-6
- Lower Secondary: Grades 7-9
- Upper Secondary: Grades 10-12

**Academic Calendar:**
- School year: November - August (10 months)
- Semester 1: November - March (5 months)
- Semester 2: April - August (5 months)
- Holidays: Khmer New Year (April), Water Festival (November)

---

## ✅ Document Status

**PRD Executive Summary:** ✅ Complete  
**Technical Architecture:** ✅ Complete  
**Database Schemas:** ✅ Complete  
**API Specifications:** ✅ Complete  
**Grading System:** ✅ Fully Documented  
**Ready for Development:** ✅ YES

---

**Last Updated:** November 19, 2025  
**Version:** 2.0  

**Ready to build! 🚀**
