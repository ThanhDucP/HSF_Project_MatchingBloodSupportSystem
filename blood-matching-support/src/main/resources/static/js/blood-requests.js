// API endpoints
const API_BASE = '/api/blood-requests';

// DOM elements
let bloodRequestsTable;
let statsElements = {};

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    initializeElements();
    loadBloodRequests();
    setupEventListeners();
});

function initializeElements() {
    bloodRequestsTable = document.getElementById('bloodRequestsTableBody');
    
    // Statistics elements
    statsElements = {
        pending: document.getElementById('pendingCount'),
        matched: document.getElementById('matchedCount'),
        confirmed: document.getElementById('confirmedCount'),
        completed: document.getElementById('completedCount')
    };
}

function setupEventListeners() {
    // Filter form submission
    const filterForm = document.getElementById('filterForm');
    if (filterForm) {
        filterForm.addEventListener('submit', function(e) {
            e.preventDefault();
            loadBloodRequests();
        });
    }
    
    // Reset filter button
    const resetButton = document.getElementById('resetFilter');
    if (resetButton) {
        resetButton.addEventListener('click', function() {
            document.getElementById('statusFilter').value = '';
            document.getElementById('bloodTypeFilter').value = '';
            document.getElementById('keywordFilter').value = '';
            loadBloodRequests();
        });
    }
}

async function loadBloodRequests() {
    try {
        showLoading(true);
        
        // Get auth token and check role
        const token = localStorage.getItem('authToken');
        const userRole = localStorage.getItem('userRole');
        
        if (!token || !userRole) {
            showError('Bạn cần đăng nhập để xem danh sách này');
            setTimeout(() => window.location.href = '/auth/login', 2000);
            return;
        }
        
        // Check role permission
        if (!['ADMIN', 'STAFF'].includes(userRole)) {
            showError('Bạn không có quyền xem danh sách này');
            return;
        }
        
        const response = await fetch(API_BASE + '/getall', {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });
        
        if (response.status === 403) {
            showError('Bạn không có quyền truy cập tính năng này');
            return;
        }
        
        if (!response.ok) {
            throw new Error('Không thể tải dữ liệu');
        }
        
        const bloodRequests = await response.json();
        
        displayBloodRequests(bloodRequests);
        updateStatistics(bloodRequests);
        
    } catch (error) {
        console.error('Error loading blood requests:', error);
        showError('Không thể tải danh sách yêu cầu máu: ' + error.message);
    } finally {
        showLoading(false);
    }
}

function displayBloodRequests(requests) {
    if (!bloodRequestsTable) return;
    
    // Update total count
    const totalCountElement = document.getElementById('totalCount');
    if (totalCountElement) {
        totalCountElement.textContent = requests.length;
    }
    
    if (requests.length === 0) {
        bloodRequestsTable.innerHTML = `
            <tr>
                <td colspan="9" class="text-center py-4">
                    <i class="fas fa-info-circle text-muted fa-2x mb-2 d-block"></i>
                    Không có yêu cầu máu nào
                </td>
            </tr>
        `;
        return;
    }
    
    bloodRequestsTable.innerHTML = requests.map(request => `
        <tr>
            <td>
                <code class="text-muted">${request.requestId}</code>
            </td>
            <td>
                <strong>${request.patientName || 'N/A'}</strong>
            </td>
            <td>
                <span class="badge bg-danger">${request.bloodType || 'N/A'}${request.rhFactor || ''}</span>
            </td>
            <td>
                <span>${request.units || 0} đơn vị</span>
            </td>
            <td>
                <span>${new Date(request.requestDate).toLocaleDateString('vi-VN')}</span>
            </td>
            <td>
                <span class="badge ${getStatusBadgeClass(request.status)}">
                    ${getStatusText(request.status)}
                </span>
            </td>
            <td class="text-center">
                <span class="badge bg-light text-dark">${request.confirmedCount || 0}</span>
                <small class="text-muted">người</small>
            </td>
            <td class="text-center">
                ${request.emergency ? '<i class="fas fa-exclamation-triangle text-warning" title="Khẩn cấp"></i>' : '<i class="fas fa-minus text-muted"></i>'}
            </td>
            <td class="text-center">
                <div class="btn-group" role="group">
                    <button class="btn btn-sm btn-outline-primary" onclick="viewDetails('${request.idBloodRequest}')" title="Xem chi tiết">
                        <i class="fas fa-eye"></i>
                    </button>
                    ${getActionButtons(request)}
                </div>
            </td>
        </tr>
    `).join('');
}

function updateStatistics(requests) {
    const stats = {
        pending: 0,
        matched: 0,
        confirmed: 0,
        completed: 0
    };
    
    requests.forEach(request => {
        const status = request.status.toLowerCase();
        if (stats.hasOwnProperty(status)) {
            stats[status]++;
        }
    });
    
    // Update DOM elements
    Object.keys(stats).forEach(status => {
        if (statsElements[status]) {
            statsElements[status].textContent = stats[status];
        }
    });
}

function getStatusBadgeClass(status) {
    const statusClasses = {
        'PENDING': 'bg-warning text-dark',
        'MATCHED': 'bg-info text-white',
        'CONFIRMED': 'bg-success text-white',
        'COMPLETED': 'bg-primary text-white'
    };
    return statusClasses[status] || 'bg-secondary text-white';
}

function getStatusText(status) {
    const statusTexts = {
        'PENDING': 'Chờ xử lý',
        'MATCHED': 'Đã ghép',
        'CONFIRMED': 'Đã xác nhận',
        'COMPLETED': 'Hoàn thành'
    };
    return statusTexts[status] || status;
}

function getActionButtons(request) {
    let buttons = '';
    
    if (request.status === 'PENDING' || request.status === 'MATCHED') {
        buttons += `
            <button type="button" class="btn btn-sm btn-outline-secondary dropdown-toggle" 
                    data-bs-toggle="dropdown" title="Thao tác khác">
                <i class="fas fa-ellipsis-v"></i>
            </button>
            <ul class="dropdown-menu">
                <li>
                    <a class="dropdown-item" href="#" onclick="updateStatus('${request.idBloodRequest}', 'CONFIRMED')">
                        <i class="fas fa-check me-2"></i>Xác nhận
                    </a>
                </li>
                <li>
                    <a class="dropdown-item text-danger" href="#" onclick="updateStatus('${request.idBloodRequest}', 'CANCELLED')">
                        <i class="fas fa-times me-2"></i>Hủy đơn
                    </a>
                </li>
            </ul>
        `;
    } else if (request.status === 'CONFIRMED') {
        buttons += `
            <button type="button" class="btn btn-sm btn-outline-secondary dropdown-toggle" 
                    data-bs-toggle="dropdown" title="Thao tác khác">
                <i class="fas fa-ellipsis-v"></i>
            </button>
            <ul class="dropdown-menu">
                <li>
                    <a class="dropdown-item" href="#" onclick="updateStatus('${request.idBloodRequest}', 'COMPLETED')">
                        <i class="fas fa-check-circle me-2"></i>Hoàn thành
                    </a>
                </li>
            </ul>
        `;
    }
    
    return buttons;
}

async function processRequest(requestId) {
    if (!confirm('Bạn có chắc muốn xử lý yêu cầu này?')) return;
    
    try {
        const response = await fetch(`${API_BASE}/process?requestId=${requestId}`, {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showSuccess(result.message || 'Xử lý thành công');
            loadBloodRequests(); // Reload data
        } else {
            showError(result.message || 'Có lỗi xảy ra');
        }
    } catch (error) {
        showError('Không thể xử lý yêu cầu: ' + error.message);
    }
}

async function updateStatus(requestId, status) {
    try {
        const response = await fetch(`${API_BASE}/update-status/${requestId}/${status}`, {
            method: 'PUT'
        });
        
        if (response.ok) {
            showSuccess('Cập nhật trạng thái thành công');
            loadBloodRequests(); // Reload data
        } else {
            const error = await response.text();
            showError(error || 'Có lỗi xảy ra');
        }
    } catch (error) {
        showError('Không thể cập nhật trạng thái: ' + error.message);
    }
}

function viewDetails(requestId) {
    // Implement view details functionality
    console.log('View details for request:', requestId);
}

function showLoading(show) {
    const loadingElement = document.getElementById('loadingSpinner');
    if (loadingElement) {
        loadingElement.style.display = show ? 'block' : 'none';
    }
}

function showSuccess(message) {
    // Create and show success toast/alert
    const alert = document.createElement('div');
    alert.className = 'alert alert-success alert-dismissible fade show position-fixed';
    alert.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.body.appendChild(alert);
    
    // Auto remove after 3 seconds
    setTimeout(() => {
        if (alert.parentNode) {
            alert.parentNode.removeChild(alert);
        }
    }, 3000);
}

function showError(message) {
    // Create and show error toast/alert
    const alert = document.createElement('div');
    alert.className = 'alert alert-danger alert-dismissible fade show position-fixed';
    alert.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.body.appendChild(alert);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (alert.parentNode) {
            alert.parentNode.removeChild(alert);
        }
    }, 5000);
}
