class RegisterForm {
    constructor(cssSelector) {
        this.form = document.querySelector(cssSelector);
        this.usernameInput = this.form.querySelector('#username');
        this.emailInput = this.form.querySelector('#email');
        this.passwordInput = this.form.querySelector('#password');
        this.confirmPasswordInput = this.form.querySelector('#confirmPassword');
    }

    submit(){
        const formData = {
            username: this.usernameInput.value,
            email: this.emailInput.value,
            password: this.passwordInput.value,
            confirmPassword: this.confirmPasswordInput.value
        };

        if(formData.username === "" || formData.email === "" || formData.password === "" || formData.confirmPassword === "") {
            showError("Vui lòng điền đầy đủ thông tin.");
            return;
        }

        const passwordPattern = /^(?=.*[A-Z])(?=.*[!@#$%^&*?])(?=.*[a-zA-Z0-9]).{6,20}$/;

        if(!passwordPattern.test(formData.password)) {
            alert("Mật khẩu phải chứa từ 6 đến 20 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt.");
            return;
        }

        if(formData.password !== formData.confirmPassword) {
            alert("Mật khẩu và xác nhận mật khẩu không khớp.");
            return;
        }

        fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
        .then(response => {
            return response.json();
        })
        .then(data => {
            if(data.code>=400) throw new Error(data.message);

            alert("Đăng ký thành công! Vui lòng đăng nhập.");
            window.location.href= '/login'; 
        })
        .catch(error => {
            alert(error.message);
        });

    }

    addSubmitListener() {
        this.form.addEventListener('submit', (event) => {
            event.preventDefault();
            this.submit();
        });
    }
}


const registerForm = new RegisterForm('#registerForm');
registerForm.addSubmitListener();