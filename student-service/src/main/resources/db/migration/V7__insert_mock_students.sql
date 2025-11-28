-- V7: Insert 50 mock students for development/testing

INSERT INTO students (id, student_code, first_name, last_name, first_name_km, last_name_km, date_of_birth, gender, address, emergency_contact, enrollment_date, status, created_at, updated_at)
VALUES
-- Students 1-10
(gen_random_uuid(), 'STU-2024-0001', 'Sokha', 'Chea', 'សុខា', 'ជា', '2010-03-15', 'M', 'Phnom Penh, Chamkarmon', '012345678', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0002', 'Sreymom', 'Keo', 'ស្រីមុំ', 'កែវ', '2011-07-22', 'F', 'Phnom Penh, Toul Kork', '012456789', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0003', 'Visal', 'Heng', 'វិសាល', 'ហេង', '2010-11-08', 'M', 'Phnom Penh, Sen Sok', '012567890', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0004', 'Chanthy', 'Sok', 'ចន្ធី', 'សុខ', '2012-01-30', 'F', 'Phnom Penh, Meanchey', '012678901', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0005', 'Dara', 'Kim', 'តារា', 'គីម', '2011-05-14', 'M', 'Kandal Province', '012789012', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0006', 'Panha', 'Chhun', 'បញ្ញា', 'ឈុន', '2010-09-25', 'M', 'Phnom Penh, Daun Penh', '012890123', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0007', 'Malis', 'Oum', 'ម៉ាលីស', 'អ៊ុំ', '2012-04-18', 'F', 'Phnom Penh, 7 Makara', '012901234', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0008', 'Rithy', 'Noun', 'រិទ្ធី', 'នួន', '2011-12-03', 'M', 'Takeo Province', '013012345', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0009', 'Bopha', 'Meng', 'បុប្ផា', 'ម៉េង', '2010-06-27', 'F', 'Phnom Penh, Russey Keo', '013123456', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0010', 'Veasna', 'Ly', 'វាសនា', 'លី', '2012-02-11', 'M', 'Kampong Cham Province', '013234567', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Students 11-20
(gen_random_uuid(), 'STU-2024-0011', 'Kunthea', 'Chan', 'គន្ធា', 'ចាន់', '2011-08-19', 'F', 'Phnom Penh, Chamkarmon', '013345678', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0012', 'Sopheak', 'Tan', 'សុភា', 'តាន់', '2010-10-05', 'M', 'Siem Reap Province', '013456789', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0013', 'Rachana', 'Prak', 'រចនា', 'ប្រាក់', '2012-03-23', 'F', 'Phnom Penh, Toul Kork', '013567890', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0014', 'Bunthoeun', 'San', 'ប៊ុនធឿន', 'សាន', '2011-01-16', 'M', 'Battambang Province', '013678901', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0015', 'Sokunthea', 'Ros', 'សុគន្ធា', 'រស់', '2010-07-09', 'F', 'Phnom Penh, Sen Sok', '013789012', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0016', 'Makara', 'Yin', 'មករា', 'យិន', '2012-05-28', 'M', 'Prey Veng Province', '013890123', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0017', 'Sreypich', 'Lim', 'ស្រីពេជ្រ', 'លឹម', '2011-11-12', 'F', 'Phnom Penh, Meanchey', '013901234', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0018', 'Piseth', 'Tep', 'ពិសិដ្ឋ', 'ទេព', '2010-04-07', 'M', 'Svay Rieng Province', '014012345', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0019', 'Sopheap', 'Chhem', 'សុភាព', 'ឈឹម', '2012-09-21', 'F', 'Phnom Penh, Daun Penh', '014123456', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0020', 'Chanthou', 'Ung', 'ចន្ធូ', 'អ៊ុង', '2011-06-14', 'M', 'Kampot Province', '014234567', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Students 21-30
(gen_random_uuid(), 'STU-2024-0021', 'Thida', 'Seng', 'ធីតា', 'សេង', '2010-12-29', 'F', 'Phnom Penh, 7 Makara', '014345678', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0022', 'Kosal', 'Nhem', 'កុសល', 'ញ៉ែម', '2012-08-03', 'M', 'Pursat Province', '014456789', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0023', 'Sreyleak', 'Pen', 'ស្រីលក្ខណ៍', 'ប៉ែន', '2011-02-26', 'F', 'Phnom Penh, Russey Keo', '014567890', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0024', 'Sovann', 'Hok', 'សុវណ្ណ', 'ហុក', '2010-05-10', 'M', 'Koh Kong Province', '014678901', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0025', 'Pisey', 'Sam', 'ពិសី', 'សំ', '2012-10-17', 'F', 'Phnom Penh, Chamkarmon', '014789012', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0026', 'Rotanak', 'Thy', 'រតនៈ', 'ធី', '2011-04-02', 'M', 'Kompong Speu Province', '014890123', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0027', 'Mealea', 'Khiev', 'មាលា', 'ខៀវ', '2010-08-24', 'F', 'Phnom Penh, Toul Kork', '014901234', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0028', 'Theara', 'Suon', 'ធារ៉ា', 'សួន', '2012-01-08', 'M', 'Kratié Province', '015012345', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0029', 'Chanra', 'Phon', 'ចន្រ្រា', 'ផន', '2011-09-30', 'F', 'Phnom Penh, Sen Sok', '015123456', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0030', 'Sambath', 'Yim', 'សម្បត្តិ', 'យឹម', '2010-03-06', 'M', 'Mondulkiri Province', '015234567', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Students 31-40
(gen_random_uuid(), 'STU-2024-0031', 'Socheata', 'Chou', 'សុជាតា', 'ជូ', '2012-06-19', 'F', 'Phnom Penh, Meanchey', '015345678', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0032', 'Vuthy', 'Kong', 'វុទ្ធី', 'គង់', '2011-12-13', 'M', 'Ratanakiri Province', '015456789', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0033', 'Srey Neang', 'Touch', 'ស្រីនាង', 'ទូច', '2010-02-27', 'F', 'Phnom Penh, Daun Penh', '015567890', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0034', 'Davith', 'Heang', 'តាវិត', 'ហ៊ាង', '2012-07-04', 'M', 'Stung Treng Province', '015678901', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0035', 'Chamnab', 'Leng', 'ចំណាប់', 'លេង', '2011-10-22', 'F', 'Phnom Penh, 7 Makara', '015789012', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0036', 'Nimol', 'Pich', 'និមល', 'ពេជ្រ', '2010-01-15', 'M', 'Preah Vihear Province', '015890123', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0037', 'Rany', 'Huy', 'រ៉ានី', 'ហុយ', '2012-04-09', 'F', 'Phnom Penh, Russey Keo', '015901234', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0038', 'Bora', 'Hin', 'បូរ៉ា', 'ហ៊ីន', '2011-08-01', 'M', 'Banteay Meanchey Province', '016012345', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0039', 'Monyrith', 'Hem', 'មុន្នីរិទ្ធ', 'ហែម', '2010-11-18', 'F', 'Phnom Penh, Chamkarmon', '016123456', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0040', 'Samnang', 'Muy', 'សំណាង', 'មួយ', '2012-02-05', 'M', 'Oddar Meanchey Province', '016234567', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Students 41-50
(gen_random_uuid(), 'STU-2024-0041', 'Reaksmey', 'Kang', 'រស្មី', 'កាង', '2011-05-29', 'F', 'Phnom Penh, Toul Kork', '016345678', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0042', 'Vatey', 'Long', 'វ៉ាតី', 'ឡុង', '2010-09-12', 'M', 'Pailin Province', '016456789', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0043', 'Sreymao', 'Chhin', 'ស្រីម៉ៅ', 'ឈិន', '2012-12-26', 'F', 'Phnom Penh, Sen Sok', '016567890', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0044', 'Ponleu', 'Sar', 'ពន្លឺ', 'សារ', '2011-03-20', 'M', 'Kep Province', '016678901', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0045', 'Leakhena', 'Sou', 'លក្ខិណា', 'សៅ', '2010-06-03', 'F', 'Phnom Penh, Meanchey', '016789012', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0046', 'Serey', 'Khem', 'សិរី', 'ខេម', '2012-09-16', 'M', 'Tbong Khmum Province', '016890123', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0047', 'Chamroeun', 'Chiv', 'ចំរើន', 'ជីវ', '2011-07-08', 'F', 'Phnom Penh, Daun Penh', '016901234', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0048', 'Thavry', 'Neou', 'ថាវរី', 'ញូ', '2010-10-31', 'M', 'Preah Sihanouk Province', '017012345', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0049', 'Leakena', 'Hean', 'លក្ខណា', 'ហៀន', '2012-05-24', 'F', 'Phnom Penh, 7 Makara', '017123456', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'STU-2024-0050', 'Ratana', 'Chea', 'រតនា', 'ជា', '2011-01-07', 'M', 'Phnom Penh, Russey Keo', '017234567', '2024-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
