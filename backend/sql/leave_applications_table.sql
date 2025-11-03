-- ============================================
-- Leave Applications Table for TopGrade App
-- ============================================

-- Create the leave_applications table if it doesn't exist
CREATE TABLE IF NOT EXISTS `leave_applications` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `campus_id` varchar(50) NOT NULL,
  `staff_id` varchar(50) NOT NULL,
  `application_title` varchar(255) NOT NULL,
  `application_body` text DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` enum('pending','approved','rejected','cancelled') DEFAULT 'pending',
  `approved_by` varchar(50) DEFAULT NULL,
  `approval_date` datetime DEFAULT NULL,
  `rejection_reason` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_campus_staff` (`campus_id`, `staff_id`),
  KEY `idx_status` (`status`),
  KEY `idx_dates` (`start_date`, `end_date`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Sample Data (Optional - for testing)
-- ============================================

-- Uncomment the lines below to insert sample data for testing
/*
INSERT INTO `leave_applications` 
(`campus_id`, `staff_id`, `application_title`, `application_body`, `start_date`, `end_date`, `status`) 
VALUES
('5c67f03e5c3da', '6876c43fd910b', 'Sick Leave', 'I am not feeling well and need rest.', '2025-11-05', '2025-11-07', 'pending'),
('5c67f03e5c3da', '6876c43fd910b', 'Family Emergency', 'Urgent family matter requires my attention.', '2025-11-10', '2025-11-12', 'approved');
*/

-- ============================================
-- Indexes for Performance
-- ============================================

-- These indexes are already defined above, but if you need to add them separately:
-- CREATE INDEX idx_campus_staff ON leave_applications(campus_id, staff_id);
-- CREATE INDEX idx_status ON leave_applications(status);
-- CREATE INDEX idx_dates ON leave_applications(start_date, end_date);
-- CREATE INDEX idx_created ON leave_applications(created_at);

-- ============================================
-- Useful Queries
-- ============================================

-- Get all pending applications for a campus
-- SELECT * FROM leave_applications WHERE campus_id = '5c67f03e5c3da' AND status = 'pending' ORDER BY created_at DESC;

-- Get all applications for a specific staff member
-- SELECT * FROM leave_applications WHERE staff_id = '6876c43fd910b' ORDER BY created_at DESC;

-- Count applications by status
-- SELECT status, COUNT(*) as count FROM leave_applications GROUP BY status;

-- Get applications within a date range
-- SELECT * FROM leave_applications WHERE start_date >= '2025-11-01' AND end_date <= '2025-11-30';

