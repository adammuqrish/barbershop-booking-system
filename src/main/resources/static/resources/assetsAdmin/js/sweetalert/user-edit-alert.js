!(function (NioApp) {
    "use strict";
  
    NioApp.sweetAlert = {
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
    }
  
    NioApp.sweetAlert.init = function () {
      NioApp.sweetAlert.confirm('.delete-account-button',{
        title: 'Are you sure?',
        text: "You won't be able to revert this!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Yes, delete it!'
      },{
        icon: 'success',
        title: 'Deleted!',
        text: 'Your account has been deleted.',
      });
    }
    
    NioApp.winLoad(NioApp.sweetAlert.init);
    
  })(NioApp);
  