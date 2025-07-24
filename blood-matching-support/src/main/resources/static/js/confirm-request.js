document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const requestId = urlParams.get('requestId');
    const token = urlParams.get('token');
    const returnUrl = urlParams.get('returnUrl') || '/';
    const authToken = localStorage.getItem('authToken');

    if (!requestId || !token) {
        showError('Thiếu thông tin xác nhận');
        return;
    }

    if (!authToken) {
        showError(`Xin hãy đăng nhập để xác nhận. <a href="/auth/login?return_url=${encodeURIComponent(window.location.href)}" class="btn btn-primary"> Đi tới đăng nhập</a>`);
        return;
    }

    function showError(message) {
        document.getElementById('confirmation-message').innerHTML = `
            <div class="alert alert-danger">
                <p>${message}</p>
            </div>
        `;
        document.getElementById('loading-spinner').style.display = 'none';
    }

    function showSuccess(message) {
        document.getElementById('confirmation-message').innerHTML = `
            <div class="alert alert-success">
                <p>${message}</p>
            </div>
            <div class="mt-3">
                <a href="${returnUrl}" class="btn btn-primary">Quay lại trang chủ</a>
            </div>
        `;
        document.getElementById('loading-spinner').style.display = 'none';
    }

    function checkAuthentication() {
        if (!authToken) {
            // Redirect to login with return_url
            const returnTo = window.location.href;
            window.location.href = `/auth/login?return_url=${encodeURIComponent(returnTo)}`;
            return;
        }

        // Send confirmation request
        fetch(`/api/blood-requests/confirm-by-token?requestId=${requestId}&token=${token}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                showSuccess('Bạn đã được xác nhận thành công! Cảm ơn bạn đã giúp đỡ');
            } else {
                showError(data.message || 'Xác nhận không thành công');
            }
        })
        .catch(error => {
            showError('Có lỗi xảy ra: ' + error.message);
        });
    }

    // Check authentication immediately
    checkAuthentication();
});
