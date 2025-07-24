// API endpoints
const API_BASE = '/api/blood-requests';

const bloodStatus = {
    PENDING: 'PENDING',
    CONFIRMED: 'CONFIRMED',
    MATCHED: 'MATCHED',
    CANCELLED: 'CANCELLED',
    COMPLETED: 'COMPLETED'
}

const bloodTypes = {
    A: 'A',
    B: 'B',
    AB: 'AB',
    O: 'O'
}

const rhFactors = {
    POSITIVE: '+',
    NEGATIVE: '-',
    POS:'+',
    NEG:'-'
}

// DOM elements
let bloodRequestsTable;
let statsElements = {};
let queryValues ={
    status: '',
    bloodType: '',
    patientName: ''
};

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
    document.getElementById('statusFilter').addEventListener('input',(e)=>{
        queryValues.status = e.target.value;
    });
    document.getElementById('bloodTypeFilter').addEventListener('input',(e)=>{
        queryValues.bloodType = e.target.value;
    });
    document.getElementById('keywordFilter').addEventListener('input',(e)=>{
        queryValues.patientName = e.target.value;
    });

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
        if (!['STAFF'].includes(userRole)&& window?.canAct) {
            showError('Bạn không có quyền xem danh sách này');
            return;
        }

        const bloodType = queryValues.bloodType? queryValues.bloodType.slice(0,queryValues.bloodType.length-1): '';
        let bloodCodeRh = queryValues.bloodType? queryValues.bloodType[queryValues.bloodType.length-1]: '';
        if(bloodCodeRh) bloodCodeRh = bloodCodeRh === '+' ? 'POSITIVE' : 'NEGATIVE';
        const response = await fetch(`${API_BASE}/${window?.canAct ? 'getall' : 'my-requests'}?${new URLSearchParams({...queryValues, bloodType, bloodCodeRh})}`, {
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
                <span class="badge bg-danger">${request.bloodCode.split("_")[0] || 'N/A'}${rhFactors[request.bloodCode.split("_")[1]] || ''}</span>
            </td>
            <td>
                <span>${request.volume || 0} ml</span>
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
            ${window?.canAct ? `
            <td class="text-center">
                <div class="btn-group" role="group">
                    ${getActionButtons(request)}
                </div>
            </td>
            `: ''}
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
            <button type="button" class="btn btn-sm btn-outline-success" 
                    title="Xác nhận" onclick="updateStatus('${request.requestId}', 'CONFIRMED')">
                <i class="fas fa-check me-2"></i>Xác nhận
            </button>
            <button type="button" class="btn btn-sm btn-outline-danger" 
                    title="Hủy đơn" onclick="updateStatus('${request.requestId}', 'CANCELLED')">
                <i class="fas fa-times me-2"></i>Hủy đơn
            </button>
        `;
    } else if (request.status === 'CONFIRMED') {
        buttons += `
            <button type="button" class="btn btn-sm btn-outline-success" 
                    title="Hoàn thành" onclick="updateStatus('${request.requestId}', 'COMPLETED')">
                <i class="fas fa-check-circle me-2"></i>Hoàn thành
            </button>
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
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken')
            },
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
