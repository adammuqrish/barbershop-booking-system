!(function (NioApp) {
  "use strict";

  NioApp.sweetAlert = {
    basic : function(selector,opt,confirmed) {
      let elm = document.querySelector(selector);
      elm.addEventListener("click", function(){
        Swal.fire({
          icon: opt.icon ? opt.icon : null,
          title: opt.title ? opt.title : null,
          text: opt.text ? opt.text : null,
          showConfirmButton: (typeof opt.showConfirmButton === 'undefined') ? true : JSON.parse(opt.showConfirmButton),
          confirmButtonText: opt.confirmButtonText ? opt.confirmButtonText : 'Ok',
          showCancelButton: (typeof opt.showCancelButton !== 'undefined') ? JSON.parse(opt.showCancelButton) : false,
          cancelButtonText: opt.cancelButtonText ? opt.cancelButtonText : 'Cancel',
          position: opt.position ? opt.position : 'center',
          timer: opt.timer ? parseInt(opt.timer) : undefined,
          timerProgressBar: opt.timerProgressBar ? opt.timerProgressBar : false,
        })
      })
    },
    confirm : function(selector,opt,confirmed) {
      let elm = document.querySelector(selector);
      elm.addEventListener("click", function(){
        Swal.fire({
          icon: opt.icon ? opt.icon : null,
          title: opt.title ? opt.title : null,
          text: opt.text ? opt.text : null,
          showConfirmButton: (typeof opt.showConfirmButton === 'undefined') ? true : JSON.parse(opt.showConfirmButton),
          confirmButtonText: opt.confirmButtonText ? opt.confirmButtonText : 'Ok',
          showCancelButton: (typeof opt.showCancelButton !== 'undefined') ? JSON.parse(opt.showCancelButton) : false,
          cancelButtonText: opt.cancelButtonText ? opt.cancelButtonText : 'Cancel',
          position: opt.position ? opt.position : 'center',
          timer: opt.timer ? parseInt(opt.timer) : undefined,
          timerProgressBar: opt.timerProgressBar ? opt.timerProgressBar : false,
        }).then((result) => {
            if (result.isConfirmed) {
              Swal.fire({
                icon: confirmed.icon ? confirmed.icon : null,
                title: confirmed.title ? confirmed.title : null,
                text: confirmed.text ? confirmed.text : null,
                showConfirmButton: (typeof confirmed.showConfirmButton === 'undefined') ? true : JSON.parse(confirmed.showConfirmButton),
                confirmButtonText: confirmed.confirmButtonText ? confirmed.confirmButtonText : 'Ok',
                showCancelButton: (typeof confirmed.showCancelButton !== 'undefined') ? JSON.parse(confirmed.showCancelButton) : false,
                cancelButtonText: confirmed.cancelButtonText ? confirmed.cancelButtonText : 'Cancel',
                position: confirmed.position ? confirmed.position : 'center',
                timer: confirmed.timer ? parseInt(confirmed.timer) : undefined,
                timerProgressBar: confirmed.timerProgressBar ? confirmed.timerProgressBar : false,
              })
            }
          })
      })
      
    },
    autoClose : function(selector,opt) {
      let elm = document.querySelector(selector);
      elm.addEventListener("click", function(){
        let timerInterval
        Swal.fire({
          icon: opt.icon ? opt.icon : null,
          title: opt.title ? opt.title : null,
          text: opt.text ? opt.text : null,
          html: opt.html ? opt.html : null,
          showConfirmButton: (typeof opt.showConfirmButton === 'undefined') ? true : JSON.parse(opt.showConfirmButton),
          confirmButtonText: opt.confirmButtonText ? opt.confirmButtonText : 'Ok',
          showCancelButton: (typeof opt.showCancelButton !== 'undefined') ? JSON.parse(opt.showCancelButton) : false,
          cancelButtonText: opt.cancelButtonText ? opt.cancelButtonText : 'Cancel',
          position: opt.position ? opt.position : 'center',
          timer: opt.timer ? parseInt(opt.timer) : undefined,
          timerProgressBar: opt.timerProgressBar ? opt.timerProgressBar : false,
          didOpen: () => {
            Swal.showLoading()
            const b = Swal.getHtmlContainer().querySelector('b')
            timerInterval = setInterval(() => {
            b.textContent = Swal.getTimerLeft()
            }, 100)
          },
          willClose: () => {
              clearInterval(timerInterval)
          }
        }).then((result) => {
          /* Read more about handling dismissals below */
            if (result.dismiss === Swal.DismissReason.timer) {
                console.log('I was closed by the timer')
            }
        })
      })
    },
    ajaxRequest : function(selector,opt) {
      let elm = document.querySelector(selector);
      elm.addEventListener("click", function(){
        let timerInterval
        Swal.fire({
          icon: opt.icon ? opt.icon : null,
          title: opt.title ? opt.title : null,
          text: opt.text ? opt.text : null,
          html: opt.html ? opt.html : null,
          showConfirmButton: (typeof opt.showConfirmButton === 'undefined') ? true : JSON.parse(opt.showConfirmButton),
          confirmButtonText: opt.confirmButtonText ? opt.confirmButtonText : 'Ok',
          showCancelButton: (typeof opt.showCancelButton !== 'undefined') ? JSON.parse(opt.showCancelButton) : false,
          cancelButtonText: opt.cancelButtonText ? opt.cancelButtonText : 'Cancel',
          position: opt.position ? opt.position : 'center',
          input: 'text',
          inputAttributes: {
            autocapitalize: 'off'
          },
          showLoaderOnConfirm: true,
          backdrop: true,
          preConfirm: (name) => {
            return fetch(`//api.github.com/users/${name}`)
              .then(response => {
                if (!response.ok) {
                  throw new Error(response.statusText)
                }
                return response.json()
              })
              .catch(error => {
                Swal.showValidationMessage(
                  `Request failed: ${error}`
                )
              })
          },
          allowOutsideClick: () => !Swal.isLoading()
        }).then((result) => {
          if (result.isConfirmed) {
            Swal.fire({
              title: `${result.value.name}'s avatar`,
              imageUrl: result.value.avatar_url
            })
          }
        })
      })
    }
  }

  NioApp.sweetAlert.init = function () {
    NioApp.sweetAlert.basic('.eg-swal-default',{
      title: 'A Simple sweet alert Content'
    });
    NioApp.sweetAlert.basic('.eg-swal-success',{
      icon: 'success',
      title: 'Good job!',
      text: "You clicked the button!",
    });
    NioApp.sweetAlert.basic('.eg-swal-info',{
      icon: 'info',
      title: 'Good job!',
      text: "You clicked the button!",
    });
    NioApp.sweetAlert.basic('.eg-swal-warning',{
      icon: 'warning',
      title: 'Are you sure?',
      text: "You won't be able to revert this!",
    });
    NioApp.sweetAlert.basic('.eg-swal-error',{
      icon: 'error',
      title: 'Oops...',
      text: 'Something went wrong!',
    });
    NioApp.sweetAlert.basic('.eg-swal-question',{
      icon: 'question',
      text:'The Internet?',
      title: 'That thing is still around?',
    });
    NioApp.sweetAlert.basic('.eg-swal-positioned',{
      position: 'top-end',
      icon: 'success',
      title: 'Your work has been saved',
      showConfirmButton: false,
      timer: 1500
    });
    NioApp.sweetAlert.confirm('.eg-swal-confirm-button',{
      title: 'Are you sure?',
      text: "You won't be able to revert this!",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete it!'
    },{
      icon: 'success',
      title: 'Deleted!',
      text: 'Your file has been deleted.',
    });
    NioApp.sweetAlert.autoClose('.eg-swal-auto-close-timer',{
      title: 'Auto close alert!',
      html: 'I will close in <b></b> milliseconds.',
      timer: 2000,
      timerProgressBar: true,
    });
    NioApp.sweetAlert.ajaxRequest('.eg-swal-ajax-request',{
      title: 'Submit your Github username',
      showCancelButton: true,
      confirmButtonText: 'Look up',
    });
  }
  
  NioApp.winLoad(NioApp.sweetAlert.init);
  
})(NioApp);
