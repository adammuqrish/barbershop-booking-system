!(function (NioApp) {
    "use strict";

    NioApp.Inbox = {
        reply : function(selector) {
            let elm = document.querySelectorAll(selector);
            elm.forEach(item =>{
                item.addEventListener('click', function(e){
                    e.preventDefault();
                    if(e.delegateTarget === undefined){
                        item.classList.toggle('is-collapsed');
                    }
                })
            })
        }
    };

    NioApp.Inbox.init = function () {
        NioApp.Inbox.reply('.toggle-reply');
    }
    NioApp.winLoad(NioApp.Inbox.init);

})(NioApp);