<?php
// IMPROVED PHP FIX for add_application operation
// Replace the entire add_application case in your api.php with this code

case 'add_application':
    try {
        // 1. Get and validate input
        $campus_id = isset($dataa_post['campus_id']) ? trim($dataa_post['campus_id']) : '';
        $staff_id = isset($dataa_post['staff_id']) ? trim($dataa_post['staff_id']) : '';
        $application_title = isset($dataa_post['application_title']) ? trim($dataa_post['application_title']) : '';
        $applictaion_body = isset($dataa_post['applictaion_body']) ? trim($dataa_post['applictaion_body']) : '';
        $start_date = isset($dataa_post['start_date']) ? trim($dataa_post['start_date']) : '';
        $end_date = isset($dataa_post['end_date']) ? trim($dataa_post['end_date']) : '';
        $category_id = isset($dataa_post['category_id']) ? $dataa_post['category_id'] : 1;

        // 2. Validate required fields
        if (empty($campus_id) || empty($staff_id) || empty($application_title) || empty($start_date) || empty($end_date)) {
            $data = Array(
                'status' => Array(
                    'code' => '2001',
                    'message' => 'Missing required fields.',
                )
            );
            header('Content-type: application/json');
            echo json_encode($data);
            die();
        }

        // 3. Parse dates - handle dd/MM/yyyy format from Android
        $start_date_obj = DateTime::createFromFormat('d/m/Y', $start_date);
        $end_date_obj = DateTime::createFromFormat('d/m/Y', $end_date);
        
        // Fallback to strtotime if DateTime parsing fails
        if ($start_date_obj === false) {
            $start_date2 = date("Y-m-d", strtotime($start_date));
        } else {
            $start_date2 = $start_date_obj->format('Y-m-d');
        }
        
        if ($end_date_obj === false) {
            $end_date2 = date("Y-m-d", strtotime($end_date));
        } else {
            $end_date2 = $end_date_obj->format('Y-m-d');
        }

        // 4. Check for duplicate submission
        $db->where('campus_id', $campus_id);
        $db->where('staff_id', $staff_id);
        $db->where('title', $application_title);
        $db->where('start_date', $start_date2);
        $db->where('end_date', $end_date2);
        $db->getOne('leave_application');

        if ($db->count > 0) {
            $data = Array(
                'status' => Array(
                    'code' => '2000',
                    'message' => 'Application Already Submitted.',
                )
            );
            header('Content-type: application/json');
            echo json_encode($data);
            die();
        }

        // 5. Get staff and campus info (with null checks)
        $staff = null;
        $camp_info = null;
        
        try {
            $db->where('campus_id', $campus_id);
            $db->where('staff_id', $staff_id);
            $staff = $db->getOne('staff');
        } catch (Exception $e) {
            error_log("Error fetching staff info: " . $e->getMessage());
        }
        
        try {
            $db->where('campus_id', $campus_id);
            $camp_info = $db->getOne('campus');
        } catch (Exception $e) {
            error_log("Error fetching campus info: " . $e->getMessage());
        }

        // 6. INSERT INTO DATABASE FIRST (most important operation)
        $data22 = Array(
            'campus_id' => $campus_id,
            'staff_id' => $staff_id,
            'title' => $application_title,
            'body' => $applictaion_body,
            'start_date' => $start_date2,
            'end_date' => $end_date2,
            'is_active' => 0,
            'category_id' => $category_id,
            'timestamp' => date('Y-m-d H:i:s')
        );
        
        $id = uniqid() . date('dmY');
        $data22['title_id'] = $id;
        
        $insert_result = $db->insert('leave_application', $data22);
        
        if (!$insert_result) {
            // Database insert failed
            $data = Array(
                'status' => Array(
                    'code' => '2002',
                    'message' => 'Failed to save application to database.',
                )
            );
            header('Content-type: application/json');
            echo json_encode($data);
            die();
        }

        // 7. Try to send notifications (non-critical - don't fail if this errors)
        try {
            if ($staff && is_array($staff) && isset($staff['full_name']) && $staff['full_name'] != '') {
                $application_msg = "Leave Application\r\nTitle: " . $application_title . 
                                 "\r\nReason: " . $applictaion_body . 
                                 "\r\nFrom: " . $staff['full_name'] . 
                                 "\r\nContact: " . (isset($staff['phone']) ? $staff['phone'] : 'N/A') .
                                 "\r\nStart: " . $start_date . 
                                 "\r\nEnd: " . $end_date;

                // Try sending email
                if ($camp_info && is_array($camp_info) && isset($camp_info['email']) && $camp_info['email'] != '') {
                    try {
                        $email_body = "<b>Reason: </b>" . $application_title . 
                                    "<br><b>Detail: </b>" . $applictaion_body . 
                                    "<br><b>From: </b>" . $staff['full_name'] .
                                    "<br><b>Email: </b>" . (isset($staff['email']) ? $staff['email'] : 'N/A') .
                                    "<br><b>Start Date: </b>" . $start_date . 
                                    "<br><b>End Date: </b>" . $end_date;
                        
                        @send_email('Application - ' . $application_title, $email_body, $camp_info['email'], false);
                    } catch (Exception $e) {
                        error_log("Email send error: " . $e->getMessage());
                    }
                }

                // Try sending SMS
                if ($camp_info && is_array($camp_info) && isset($camp_info['sms_sender']) && isset($camp_info['leave_no'])) {
                    try {
                        $sender1 = explode(',', $camp_info['sms_sender']);
                        
                        if (strpos($camp_info['leave_no'], ',') !== false) {
                            $number = explode(',', $camp_info['leave_no']);
                            foreach ($number as $value) {
                                if (!empty($value)) {
                                    @send_sms($value, $application_msg, $sender1[0], 
                                            isset($camp_info['sms_password']) ? $camp_info['sms_password'] : '', 
                                            isset($camp_info['unique_id']) ? $camp_info['unique_id'] : '');
                                }
                            }
                        } else {
                            if (!empty($camp_info['leave_no'])) {
                                @send_sms($camp_info['leave_no'], $application_msg, $sender1[0], 
                                        isset($camp_info['sms_password']) ? $camp_info['sms_password'] : '', 
                                        isset($camp_info['unique_id']) ? $camp_info['unique_id'] : '');
                            }
                        }

                        // Send to landline if configured
                        if (isset($camp_info['landline']) && $camp_info['landline'] != '' && 
                            isset($camp_info['sms_alt_number']) && $camp_info['sms_alt_number'] == 1) {
                            @send_sms($camp_info['landline'], $application_msg, $sender1[0], 
                                    isset($camp_info['sms_password']) ? $camp_info['sms_password'] : '', 
                                    isset($camp_info['unique_id']) ? $camp_info['unique_id'] : '');
                        }
                    } catch (Exception $e) {
                        error_log("SMS send error: " . $e->getMessage());
                    }
                }
            }
        } catch (Exception $e) {
            // Log but don't fail - notifications are non-critical
            error_log("Notification error: " . $e->getMessage());
        }

        // 8. ALWAYS send success response (database insert was successful)
        $data = Array(
            'status' => Array(
                'code' => '1000',
                'message' => 'Application Submit.',
            )
        );
        header('Content-type: application/json');
        echo json_encode($data);
        die();
        
    } catch (Exception $e) {
        // Catch any unexpected errors
        error_log("Leave application error: " . $e->getMessage());
        $data = Array(
            'status' => Array(
                'code' => '2003',
                'message' => 'Server error: ' . $e->getMessage(),
            )
        );
        header('Content-type: application/json');
        echo json_encode($data);
        die();
    }
    break;
?>

