!(function (NioApp) {
    "use strict";
    let selectors = {
        chatBodyId: 'chatBody',
        chatItem: 'toggle-chat-item',
        chatClose: 'toggle-close-chat',
    }
    NioApp.Chat = {};
    NioApp.Chat = {
        open: function(selector){
            let elm = document.querySelectorAll(`.${selectors.chatItem}`);
            elm.forEach(item =>{
                let targetElm = document.getElementById(selectors.chatBodyId);
                item.addEventListener('click', function(e){
                    e.preventDefault();
                    elm.forEach(item => {
                        item.classList.remove('active');
                    })
                    item.classList.add('active');
                    targetElm.classList.add('show-chat');
                })
            })
        },
        close: function(){
            let elm = document.querySelectorAll(`.${selectors.chatClose}`);
            let chatItems = document.querySelectorAll(`.${selectors.chatItem}`);
            elm.forEach(item =>{
                let targetElm = document.getElementById(selectors.chatBodyId);
                item.addEventListener('click', function(e){
                    e.preventDefault();
                    chatItems.forEach(item => {
                        item.classList.remove('active');
                    })
                    targetElm.classList.remove('show-chat');
                })
            })
        }
    };

    NioApp.Chat.init = function () {
        NioApp.Chat.open();
        NioApp.Chat.close();
    }
    NioApp.winLoad(NioApp.Chat.init);

})(NioApp);