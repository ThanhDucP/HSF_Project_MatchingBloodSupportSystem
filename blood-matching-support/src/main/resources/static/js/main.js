// Main JavaScript for Blood Matching Support

document.addEventListener('DOMContentLoaded', function() {
    // Initialize all components
    initializeTooltips();
    initializeAlerts();
    initializeFormValidation();
    initializeProgressBars();
    initializeRealtimeUpdates();
    checkAuthStatus();
});

// Check authentication status
function checkAuthStatus() {
    const token = localStorage.getItem('authToken');
    const authLinks = document.querySelectorAll('[data-auth-required]');
    const guestLinks = document.querySelectorAll('[data-guest-only]');
    
    if (token) {
        // User is logged in
        //The data-auth-reuired attribute need to be empty to show the element or if it is not then the role of the user must match
        const userRole = localStorage.getItem('userRole') || '';
        authLinks.forEach(link => {
            if (!link.attributes["data-auth-required"].value || link.attributes["data-auth-required"].value === userRole) {
                link.style.display = 'block';
            }
        });
        guestLinks.forEach(link => link.style.display = 'none');
    } else {
        // User is not logged in
        authLinks.forEach(link => link.style.display = 'none');
        guestLinks.forEach(link => link.style.display = 'block');
    }
}

// Add JWT token to API requests
function makeAuthenticatedRequest(url, options = {}) {
    const token = localStorage.getItem('authToken');
    
    if (token) {
        options.headers = {
            ...options.headers,
            'Authorization': `Bearer ${token}`
        };
    }
    
    return fetch(url, options);
}

function initializeLogout(){
    
}

// Logout function
function logout() {
    const token = localStorage.getItem('authToken');
    
    if (token) {
        // Call logout API
        fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
    
    // Clear local storage
    localStorage.removeItem('authToken');
    localStorage.removeItem('accountId');
    
    // Redirect to home
    window.location.href = '/';
}

// Initialize Bootstrap tooltips
function initializeTooltips() {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Auto-hide alerts after 5 seconds
function initializeAlerts() {
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(alert => {
        setTimeout(() => {
            if (alert && alert.parentNode) {
                alert.classList.add('fade');
                setTimeout(() => {
                    alert.remove();
                }, 300);
            }
        }, 5000);
    });
}

// Form validation enhancements
function initializeFormValidation() {
    const forms = document.querySelectorAll('form[novalidate]');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                
                // Focus on first invalid field
                const firstInvalidField = form.querySelector(':invalid');
                if (firstInvalidField) {
                    firstInvalidField.focus();
                }
            }
            
            form.classList.add('was-validated');
        });
        
        // Real-time validation
        const inputs = form.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            input.addEventListener('blur', function() {
                if (input.checkValidity()) {
                    input.classList.remove('is-invalid');
                    input.classList.add('is-valid');
                } else {
                    input.classList.remove('is-valid');
                    input.classList.add('is-invalid');
                }
            });
        });
    });
}

// Progress bar animations
function initializeProgressBars() {
    const progressBars = document.querySelectorAll('.progress-bar');
    
    progressBars.forEach(bar => {
        const targetWidth = bar.getAttribute('aria-valuenow');
        const maxWidth = bar.getAttribute('aria-valuemax');
        const percentage = (targetWidth / maxWidth) * 100;
        
        // Animate progress bar
        setTimeout(() => {
            bar.style.width = percentage + '%';
        }, 300);
    });
}

// Real-time updates for blood request status
function initializeRealtimeUpdates() {
    const statusElements = document.querySelectorAll('[data-request-id]');
    
    if (statusElements.length > 0) {
        // Update every 30 seconds
        setInterval(updateRequestStatuses, 30000);
    }
}

function updateRequestStatuses() {
    const statusElements = document.querySelectorAll('[data-request-id]');
    
    statusElements.forEach(element => {
        const requestId = element.getAttribute('data-request-id');
        
        fetch(`/api/blood-requests/${requestId}/status`)
            .then(response => response.json())
            .then(data => {
                updateStatusDisplay(element, data.status, data.confirmedCount);
            })
            .catch(error => {
                console.warn('Failed to update status for request:', requestId, error);
            });
    });
}

function updateStatusDisplay(element, status, confirmedCount) {
    const statusBadge = element.querySelector('.status-badge');
    const confirmCountElement = element.querySelector('.confirm-count');
    
    if (statusBadge) {
        // Update status badge
        statusBadge.className = `badge status-badge ${getStatusBadgeClass(status)}`;
        statusBadge.textContent = getStatusDisplayText(status);
    }
    
    if (confirmCountElement && confirmedCount !== undefined) {
        confirmCountElement.textContent = confirmedCount;
        
        // Add animation if count increased
        if (parseInt(confirmCountElement.textContent) > confirmedCount) {
            confirmCountElement.classList.add('text-success');
            setTimeout(() => {
                confirmCountElement.classList.remove('text-success');
            }, 2000);
        }
    }
}

function getStatusBadgeClass(status) {
    const statusClasses = {
        'PENDING': 'bg-warning text-dark',
        'MATCHED': 'bg-info',
        'CONFIRMED': 'bg-primary',
        'COMPLETED': 'bg-success',
        'CANCELLED': 'bg-secondary'
    };
    
    return statusClasses[status] || 'bg-secondary';
}

function getStatusDisplayText(status) {
    const statusTexts = {
        'PENDING': 'Đang chờ',
        'MATCHED': 'Đã ghép',
        'CONFIRMED': 'Đã xác nhận',
        'COMPLETED': 'Hoàn thành',
        'CANCELLED': 'Đã hủy'
    };
    
    return statusTexts[status] || status;
}

// Blood compatibility checker
function checkBloodCompatibility(donorType, recipientType) {
    const compatibility = {
        'O-': ['O-', 'O+', 'A-', 'A+', 'B-', 'B+', 'AB-', 'AB+'],
        'O+': ['O+', 'A+', 'B+', 'AB+'],
        'A-': ['A-', 'A+', 'AB-', 'AB+'],
        'A+': ['A+', 'AB+'],
        'B-': ['B-', 'B+', 'AB-', 'AB+'],
        'B+': ['B+', 'AB+'],
        'AB-': ['AB-', 'AB+'],
        'AB+': ['AB+']
    };
    
    return compatibility[donorType]?.includes(recipientType) || false;
}

// Notification system
function showNotification(message, type = 'info', duration = 5000) {
    const notificationContainer = getOrCreateNotificationContainer();
    
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} alert-dismissible fade show`;
    notification.innerHTML = `
        <i class="fas fa-${getNotificationIcon(type)} me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    notificationContainer.appendChild(notification);
    
    // Auto-dismiss
    setTimeout(() => {
        if (notification.parentNode) {
            notification.classList.remove('show');
            setTimeout(() => {
                notification.remove();
            }, 300);
        }
    }, duration);
}

function getNotificationIcon(type) {
    const icons = {
        'success': 'check-circle',
        'danger': 'exclamation-triangle',
        'warning': 'exclamation-circle',
        'info': 'info-circle'
    };
    
    return icons[type] || 'info-circle';
}

function getOrCreateNotificationContainer() {
    let container = document.getElementById('notification-container');
    
    if (!container) {
        container = document.createElement('div');
        container.id = 'notification-container';
        container.className = 'position-fixed top-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }
    
    return container;
}

// Utility functions
function formatDate(dateString, format = 'dd/MM/yyyy') {
    const date = new Date(dateString);
    
    if (format === 'dd/MM/yyyy') {
        return date.toLocaleDateString('vi-VN');
    } else if (format === 'relative') {
        return getRelativeTimeString(date);
    }
    
    return date.toLocaleDateString();
}

function getRelativeTimeString(date) {
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) {
        return 'Vừa xong';
    } else if (diffInSeconds < 3600) {
        return `${Math.floor(diffInSeconds / 60)} phút trước`;
    } else if (diffInSeconds < 86400) {
        return `${Math.floor(diffInSeconds / 3600)} giờ trước`;
    } else if (diffInSeconds < 2592000) {
        return `${Math.floor(diffInSeconds / 86400)} ngày trước`;
    } else {
        return date.toLocaleDateString('vi-VN');
    }
}

// Search and filter helpers
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Export functions for global use
window.BloodMatchingApp = {
    showNotification,
    checkBloodCompatibility,
    formatDate,
    updateRequestStatuses,
    debounce
};
