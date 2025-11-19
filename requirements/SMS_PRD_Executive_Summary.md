# Product Requirements Document
# Student Management System for Cambodia Public Schools
## Executive Version

**Version:** 1.0  
**Date:** November 19, 2025  
**Status:** Draft  

---

## 1. Executive Summary

### The Opportunity
Cambodia's 90,000+ public school teachers spend 30-60 minutes daily on manual record-keeping. This inefficiency costs the education system approximately 4.5 million hours annually—time that could be spent teaching.

**The Solution:** A responsive web application enabling teachers to digitally manage students, track attendance, record grades, and generate MoEYS-compliant reports—accessible from any device with an internet connection.

**Market Opportunity:** $4.5M annual addressable market (90,000 teachers × $50/year)

**Year 1 Target:** 1,000 teachers | $29K ARR | 70% time savings

---

## 2. Core Product Features

### Student Management
- Digital profiles with photos and family contacts
- **Complete enrollment history tracking** (handles class changes and transfers)
- Enrollment tracking and transfer management  
- Bulk CSV import for efficient setup
- Multi-year history retention

### Attendance Tracking  
- One-tap daily marking (5 status types)
- Calendar view and history
- At-risk student alerts (<80% attendance)
- Real-time data synchronization

### Grade Management
- MoEYS curriculum-aligned subjects
- **Flexible assessment structure:**
  - 4 monthly exams per semester (customizable)
  - 1 semester exam per semester
  - Simple averaging (no complex weighting)
- Automated average calculations
- Class ranking and analytics
- Historical performance tracking
- Teacher-configurable assessment schedule

### Report Generation
- **Three report types:**
  - Monthly progress reports (track ongoing performance)
  - Semester report cards (comprehensive mid/end semester)
  - Annual transcripts (full year summary with promotion status)
- MoEYS-compliant format
- Generate in <30 seconds per student
- Bulk generation for entire class
- Khmer/English language options
- Digital signatures ready

### Communication (Premium)
- SMS notifications to parents
- Automated absence alerts  
- Announcement broadcasting

---

## 3. User Personas

### Sreymom (32) - Urban Teacher
- **Profile:** 8 years experience, 45 students, moderate tech comfort
- **Pain:** 45 min/day on paperwork, manual calculations error-prone
- **Goal:** "I want to focus on teaching, not paperwork"
- **Device:** Mid-range Android smartphone

### Sokha (28) - Rural Teacher  
- **Profile:** 3 years experience, 35 students, basic tech skills
- **Pain:** Time-consuming manual processes, limited digital tools training
- **Goal:** "Something simple that saves me time"
- **Device:** Budget Android smartphone (2019)

### Vanna (48) - Senior Teacher
- **Profile:** 22 years experience, hesitant about technology
- **Pain:** Comfortable with paper, worried about complexity
- **Goal:** "If it really saves time, I'll try it"
- **Device:** Basic smartphone

---

## 4. Technical Architecture

**Frontend:** Next.js 14 Web App | React | TypeScript | TailwindCSS | Responsive Design (Mobile & Desktop)

**Backend:** Spring Boot microservices | PostgreSQL | Redis cache | RabbitMQ

**Microservices:**
- API Gateway (8080) → Auth (8081) → Student (8082) → Attendance (8083) → Grade (8084) → Report (8085) → Notification (8086)

**Infrastructure:** Docker | Docker Compose | Cloud hosting (AWS/GCP/DigitalOcean)

**Deployment:**
- **Development:** Docker Compose on local machine
- **Staging:** Docker Compose on staging server
- **Production:** Docker Compose with reverse proxy (Nginx)
- Database: PostgreSQL containers with persistent volumes
- Caching: Redis container
- Message Queue: RabbitMQ container
- Load Balancing: Nginx container

**Key Technical Features:**
- Responsive design (works on mobile, tablet, desktop)
- Server-side rendering (SSR) with Next.js for fast page loads
- Real-time data updates via React Query
- Optimized for 3G/4G networks (compressed assets, lazy loading)
- Session-based authentication with Redis
- **Robust enrollment tracking** (complete history of class changes and transfers)

---

## 5. Business Model

### Freemium Pricing

**Free Tier:**
- 50 students max
- Core features (attendance, grades, reports)
- Responsive web access (mobile & desktop)
- Khmer language

**Premium ($2-5/month):**
- Unlimited students
- SMS notifications  
- Advanced analytics
- Priority support
- Custom reports

### Revenue Streams

1. **Individual Subscriptions** - Teachers pay directly
2. **School Licenses** - Bulk pricing for entire schools  
3. **NGO Partnerships** - Subsidized for underserved schools
4. **Institutional Sales** - MoEYS pilot programs

### Financial Projections

**Year 1:**
- Users: 1,000 (850 free + 150 premium)
- ARR: $29K (optimistic with partnerships)
- MRR: $2,400

**Year 2:**  
- Users: 10,000 (10% premium)
- ARR: $101K
- Path to profitability

---

## 6. Success Metrics

### North Star Metric
**Teacher Time Saved:** 70% reduction (45 min → 13 min daily)

### Key KPIs

**Acquisition:**
- 1,000 registered teachers (Year 1)
- 70% activation rate

**Engagement:**
- 80% weekly active users
- 90% mark attendance ≥3 days/week

**Retention:**
- 60% retention at 90 days
- 50% retention at 180 days

**Revenue:**
- 15% premium conversion
- <5% monthly churn
- $29K ARR (Year 1)

**Satisfaction:**
- NPS ≥40
- CSAT ≥4.0/5.0

**Technical:**
- 99.5% uptime
- <500ms API response (p95)
- <2 sec page load on 3G

---

## 7. Product Roadmap

### Phase 1: MVP (Months 1-3)
**Goal:** 50 pilot teachers

- User authentication
- Student & class management
- Daily attendance
- Basic grade entry
- PDF reports
- Responsive design (mobile & desktop)
- Khmer language

**Success:** 70% mark attendance ≥3 days/week

### Phase 2: Enhancement (Months 4-6)  
**Goal:** 200 teachers

- MoEYS-compliant reports
- Bulk generation
- Analytics dashboard
- CSV import
- Help center (Khmer videos)

**Success:** <30 sec report generation, 60% retention

### Phase 3: Premium (Months 7-9)
**Goal:** 1,000 teachers, monetization

- SMS notifications (premium)
- Payment integration
- Subscription management
- Parent portal (basic)

**Success:** 15% premium conversion, $2,400 MRR

### Phase 4: Scale (Months 10-12)
**Goal:** 2,000 teachers, partnerships

- School admin dashboard
- MoEYS data export
- Provincial expansion
- 3+ NGO partnerships

**Success:** 30% rural teachers, $8K MRR

---

## 8. Key Risks & Mitigation

### Technical Risks

**1. Internet Connectivity Issues** (Medium/Medium)
- *Mitigation:* Optimize for slow networks, progressive loading, clear error messages, session persistence

**2. Performance at Scale** (Medium/High)  
- *Mitigation:* Redis caching, auto-scaling, load testing, CDN for static assets

**3. Data Loss** (Low/Critical)
- *Mitigation:* Daily backups, replication, monthly restore tests

### Business Risks

**4. Low Adoption** (Medium/High)
- *Mitigation:* Simple UX, free tier, training, rapid iteration

**5. Insufficient Funding** (Medium/Critical)
- *Mitigation:* Bootstrap, seed funding, grants, early premium launch

**6. Competition** (Low/High)
- *Mitigation:* Cambodia focus, partnerships, faster iteration

### Market Risks

**7. MoEYS Policy Change** (Low/Critical)  
- *Mitigation:* Early engagement, pilot program, flexibility

**8. Cultural Resistance** (Medium/Medium)
- *Mitigation:* Start with early adopters, peer testimonials, respect tradition

---

## 9. Investment Requirements

### Year 1 Budget: $274,500

**Development (Months 1-6): $109,500**
- Team: $90K (PM, 2 devs, designer, QA)
- Infrastructure: $4.5K  
- Legal & marketing: $5K
- Contingency: $10K

**Growth (Months 7-12): $165,000**
- Team: $123K (add dev + support)
- Infrastructure: $14K (scaling)
- Marketing: $13K
- Contingency: $15K

### Funding Strategy

1. Bootstrap: $50K (founders)
2. Seed Round: $150K (angel/VC)
3. Grants: $50K (education tech)
4. Revenue: $24.5K (Year 1 ARR)

**Runway:** 12-15 months

---

## 10. Investment Opportunity

### Seeking: $150,000 Seed Funding

**Use of Funds:**
- 60% Team (world-class product & engineering)
- 20% Infrastructure & technology
- 15% Marketing & acquisition  
- 5% Legal & contingency

**Terms:**  
- Convertible note or SAFE
- Valuation cap: $1.5M  
- Discount: 20%

**Expected Returns:**
- Year 2 (Series A): $5-10M valuation (3-7x)
- Year 5 exit: $20-50M potential

### Why This Will Succeed

**✓ Real problem:** Teachers universally frustrated with manual processes  
**✓ Proven demand:** Pilot feedback validates strong interest  
**✓ Cambodia-specific:** Khmer language, MoEYS curriculum-aligned, mobile-friendly  
**✓ No dominant competitor:** Open market opportunity  
**✓ Scalable model:** Low marginal cost, multiple revenue streams  
**✓ Social impact:** Improving education for millions of students

### Traction & Validation

- 50+ teachers interested in pilot
- 2 NGO partners committed to distribution
- MoEYS officials expressing support
- Technical prototype validated

---

## 11. Next Steps

### Immediate Actions (Week 1-4)

**Week 1-2:**
- Finalize team hiring
- Development environment setup
- UI/UX wireframes
- Sprint planning

**Week 3-4:**
- Core authentication development
- Database design
- CI/CD pipeline
- Landing page launch

### Month 1-3: MVP Development

- Build core features  
- Implement offline functionality
- Khmer localization
- Beta testing (10 teachers)
- Pilot recruitment (50 teachers)

### Month 4+: Scale & Iterate

- Official launch
- Feedback collection
- Feature iteration
- Partnership development
- Premium tier prep

---

## 12. Long-Term Vision

### 3 Years
- 30,000 teachers (30% Cambodia market)
- Market leader in Cambodia EdTech
- Expand to school management features
- Enter neighboring countries (Laos, Myanmar)

### 5 Years  
- 100,000+ teachers across Southeast Asia
- Regional EdTech leader
- Platform for education innovation
- Strategic acquisition or IPO path

### Potential Acquirers
- International EdTech companies (expanding to Asia)
- Regional tech companies (Grab, Gojek services)
- Education publishers (Pearson, McGraw Hill)
- Mission-driven foundations

---

## Contact & Investment Inquiries

**Investment Deck:** Available upon request  
**Product Demo:** Schedule at calendly.com/sms-demo

**Email:** invest@sms.com.kh  
**Website:** www.sms.com.kh  
**Phone:** +855 XX XXX XXXX

**Social:**
- Facebook: facebook.com/smsCambodia
- LinkedIn: linkedin.com/company/sms-cambodia

---

## Appendix: Key Differentiators

| Feature | Paper System | Generic Software | **Our Solution** |
|---------|-------------|------------------|------------------|
| Responsive Design | N/A | ⚠️ Desktop-focused | **✅ Mobile & Desktop** |
| Khmer Language | ✅ | ⚠️ Limited | **✅ Complete** |
| MoEYS Curriculum | ❌ Manual | ❌ Generic | **✅ Built-in 6+3+3** |
| Easy Setup | N/A | ❌ Complex | **✅ Docker-based** |
| Teacher Focus | N/A | ❌ Admin-centric | **✅ Teacher-centric** |
| Price | Free | $$$ | **$ Affordable** |
| Cambodia-Optimized | N/A | ❌ Generic | **✅ Localized & optimized** |

---

**This document is confidential and proprietary.**  
**© 2025 Student Management System Cambodia. All rights reserved.**

---

**END OF EXECUTIVE SUMMARY**  
**Pages: 12 | Words: ~4,500**
