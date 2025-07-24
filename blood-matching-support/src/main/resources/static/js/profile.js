class ProfileForm{
    constructor(cssSelector){
        this.form = document.querySelector(cssSelector);
        this.nameInput = this.form.querySelector('#name');
        this.phoneInput = this.form.querySelector('#phone');
        this.dobInput = this.form.querySelector('#dob');
        this.genderInput = this.form.querySelector('#gender');
        this.bloodTypeInput = this.form.querySelector('#bloodType');
        this.rhFactorInput = this.form.querySelector('#rhFactor');
        this.cityInput = this.form.querySelector('#city');
        this.districtInput = this.form.querySelector('#district');
        this.wardInput = this.form.querySelector('#ward');
        this.streetInput = this.form.querySelector('#street');
        this.latitudeInput = this.form.querySelector('#latitude');
        this.longitudeInput = this.form.querySelector('#longitude');
    }

    fetchInitialData(){
        fetch("/api/profile/",{
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken'),
                'Content-Type': 'application/json'
            },
        }).then(response => response.json()).then(data => {
            const profile = data.result;
            this.nameInput.value = profile.name||'';
            this.phoneInput.value = profile.phone||'';
            this.dobInput.value = profile.dob;
            this.genderInput.value = profile.gender;
            this.bloodTypeInput.value = profile.blood.bloodType.split("_")[0];
            this.rhFactorInput.value = profile.blood.rhFactor;
            this.cityInput.value = profile.address.city;
            this.districtInput.value = profile.address.district;
            this.wardInput.value = profile.address.ward;
            this.streetInput.value = profile.address.street;
            this.latitudeInput.value = profile.address.latitude;
            this.longitudeInput.value = profile.address.longitude;
        })
    }

    submit(){
        const formData = {
            name: this.nameInput.value,
            phone: this.phoneInput.value,
            dob: this.dobInput.value,
            gender: this.genderInput.value,
            blood:{
                bloodType: this.bloodTypeInput.value,
                rhFactor: this.rhFactorInput.value,
            },
            address:{
            city: this.cityInput.value,
                district: this.districtInput.value,
                ward: this.wardInput.value,
                street: this.streetInput.value,
                latitude: this.latitudeInput.value,
                longitude: this.longitudeInput.value
            }
        };


        fetch("/api/profile/save",{
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('authToken'),
                'Content-Type': 'application/json'
            },
            method: 'PUT',
            body: JSON.stringify(formData)
        }).then(response=>{
            if(response.ok){
                return response.json()
            }
            else{
                throw new Error(response.text());
            }
        }).then(data=>{
            if(data.code>=400) throw new Error(data.message);
            alert("Cập nhật thông tin thành công");
        })
        .catch(error=>{
            console.log(error);
            alert("Cập nhật thông tin thất bại");
        })
    }

    addSubmitListener(){
        this.form.addEventListener('submit', (event) => {
            event.preventDefault();
            this.submit();
        });
    }
}

const profileForm = new ProfileForm('#profileForm');
profileForm.fetchInitialData();
profileForm.addSubmitListener();
