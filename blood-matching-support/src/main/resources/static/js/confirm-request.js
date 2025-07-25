document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    const requestId = urlParams.get('requestId');
    const token = urlParams.get('token');
    const returnUrl = urlParams.get('returnUrl') || '/';

    if (!requestId || !token) {
        showError('Thiếu thông tin xác nhận');
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

    // Gửi xác nhận KHÔNG CẦN AUTH TOKEN
    fetch(`/api/blood-requests/confirm-by-token?requestId=${requestId}&token=${token}`, {
        method: 'PUT'
    })
        .then(async response => {
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                const data = await response.json();
                if (data.status === 'success') {
                    showSuccess('✅ Bạn đã xác nhận đơn hiến máu thành công. Xin cảm ơn bạn đã giúp đỡ!');
                } else {
                    showError(data.message || '❌ Xác nhận không thành công.');
                }
            } else {
                const text = await response.text();
                showError(text || '❌ Xác nhận không thành công.');
            }
        })
        .catch(error => {
            showError('❌ Có lỗi xảy ra: ' + error.message);
        });
});
