!(function (NioApp) {
    "use strict";
    
    NioApp.Tinymce = function (selector, opt) {
      let elm = document.querySelectorAll(selector);
        if( elm != 'undefined' && elm != null ){
          elm.forEach(item => {
            let itemId = item.id;
            let toolbar = item.dataset.toolbar ? JSON.parse(item.dataset.toolbar) : true;
            let menubar = item.dataset.menubar ? JSON.parse(item.dataset.menubar) : true;
            let inline = item.dataset.inline ? JSON.parse(item.dataset.inline) : false;
            let height = item.dataset.height ? parseInt(item.dataset.height) : 300;
            tinymce.init({
                selector: `#${itemId}`,
                content_css: false,
                height: height,
                skin: false,
                branding: false,
                toolbar: toolbar,
                menubar: menubar,
                inline: inline
            });
          })
        }
    }
    
    NioApp.Tinymce.init = function () {
      NioApp.Tinymce('.js-tinymce')
    }
    
    NioApp.winLoad(NioApp.Tinymce.init);
    
    })(NioApp);
    