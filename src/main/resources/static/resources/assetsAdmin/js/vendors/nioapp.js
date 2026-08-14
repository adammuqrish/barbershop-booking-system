/*!
  * NioApp v1.0.0 (https://softnio.com/)
  * Developed by Softnio Team.
  * Copyright by Softnio.
  */
var NioApp = (function ( win, doc) {
    "use strict";
    var NioApp = { 
        AppInfo: { 
            name: "NioApp", version: "1.0.0", author: "Softnio" 
        }, 
        Package: { 
            name: "NioBoard", version: "1.0", 
        } 
    }

    function docReady(callback){
        document.addEventListener('DOMContentLoaded', callback, false);
    }
    
    function winLoad(callback){
        window.addEventListener('load', callback, false);
    }
    
    function onResize(callback,selector){
        selector = (typeof selector === typeof undefined) ? window : selector;
        selector.addEventListener('resize', callback)
    }
    
    NioApp.docReady = docReady;
    NioApp.winLoad = winLoad;
    NioApp.onResize = onResize;

    return NioApp;
}(window, document));

NioApp = function (NioApp) {
    "use strict";

    //Get Value For Custom PropertyValue  @v1.0
    
    // Global Uses @v1.0
    /////////////////////////////
    NioApp.BS = {};
    NioApp.Addons = {};
    NioApp.Custom = {};
    NioApp.Toggle = {};
    NioApp.body = document.querySelector('body');
    NioApp.Win = { height: window.innerHeight, width: window.innerWidth };
    NioApp.Break = { mb: 420, sm: 576, md: 768, lg: 992, xl: 1200, xxl: 1400, any: Infinity };
    NioApp.isDark = (NioApp.body.classList.contains('dark-mode')) ? true : false;
    NioApp.monthList = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
    NioApp.docStyle = getComputedStyle(document.documentElement);

    // Colors Css 
    NioApp.Colors = {
        blue: NioApp.docStyle.getPropertyValue('--bs-blue'),
        indigo: NioApp.docStyle.getPropertyValue('--bs-indigo'),
        purple: NioApp.docStyle.getPropertyValue('--bs-purple'),
        pink: NioApp.docStyle.getPropertyValue('--bs-pink'),
        red: NioApp.docStyle.getPropertyValue('--bs-red'),
        orange: NioApp.docStyle.getPropertyValue('--bs-orange'),
        yellow: NioApp.docStyle.getPropertyValue('--bs-yellow'),
        green: NioApp.docStyle.getPropertyValue('--bs-green'),
        teal: NioApp.docStyle.getPropertyValue('--bs-teal'),
        cyan: NioApp.docStyle.getPropertyValue('--bs-cyan'),
        black: NioApp.docStyle.getPropertyValue('--bs-black'),
        white: NioApp.docStyle.getPropertyValue('--bs-white'),
        gray: NioApp.docStyle.getPropertyValue('--bs-gray'),
        grayDark: NioApp.docStyle.getPropertyValue('--bs-gray-dark'),
        gray100: NioApp.docStyle.getPropertyValue('--bs-gray-100'),
        gray200: NioApp.docStyle.getPropertyValue('--bs-gray-200'),
        gray300: NioApp.docStyle.getPropertyValue('--bs-gray-300'),
        gray400: NioApp.docStyle.getPropertyValue('--bs-gray-400'),
        gray500: NioApp.docStyle.getPropertyValue('--bs-gray-500'),
        gray600: NioApp.docStyle.getPropertyValue('--bs-gray-600'),
        gray700: NioApp.docStyle.getPropertyValue('--bs-gray-700'),
        gray800: NioApp.docStyle.getPropertyValue('--bs-gray-800'),
        gray900: NioApp.docStyle.getPropertyValue('--bs-gray-900'),
        primary: NioApp.docStyle.getPropertyValue('--bs-primary'),
        secondary: NioApp.docStyle.getPropertyValue('--bs-secondary'),
        success: NioApp.docStyle.getPropertyValue('--bs-success'),
        info: NioApp.docStyle.getPropertyValue('--bs-info'),
        warning: NioApp.docStyle.getPropertyValue('--bs-warning'),
        danger: NioApp.docStyle.getPropertyValue('--bs-danger'),
        light: NioApp.docStyle.getPropertyValue('--bs-light'),
        dark: NioApp.docStyle.getPropertyValue('--bs-dark'),
        darker: NioApp.docStyle.getPropertyValue('--bs-darker'),
        bodyColor: NioApp.docStyle.getPropertyValue('--bs-body-color'),
        bodyBg: NioApp.docStyle.getPropertyValue('--bs-body-bg'),
        borderColor: NioApp.docStyle.getPropertyValue('--bs-border-color'),
        borderColorTranslucent: NioApp.docStyle.getPropertyValue('--bs-border-color-translucent'),
        headingColor: NioApp.docStyle.getPropertyValue('--bs-heading-color'),
        linkColor: NioApp.docStyle.getPropertyValue('--bs-link-color'),
        linkHoverColor: NioApp.docStyle.getPropertyValue('--bs-link-hover-color'),
        codeColor: NioApp.docStyle.getPropertyValue('--bs-code-color'),
        highlightBg: NioApp.docStyle.getPropertyValue('--bs-highlight-bg'),
    }
    
    // State @v1.0
    NioApp.State = {
        isRTL: (NioApp.body.classList.contains('has-rtl') || NioApp.body.getAttribute('dir') === 'rtl') ? true : false,
        isTouch: (("ontouchstart" in document.documentElement)) ? true : false,
        isMobile: (navigator.userAgent.match(/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini|Windows Phone|/i)) ? true : false,
        asMobile: (NioApp.Win.width < NioApp.Break.md) ? true : false
    };

    // State Update @v1.1
    NioApp.StateUpdate = function () {
        NioApp.Win = { height: window.innerHeight, width: window.innerWidth };
        NioApp.State.asMobile = (NioApp.Win.width < NioApp.Break.md) ? true : false;
    };

    
    ///////////////////////////////
    //Functions 1.0
    /////////////////////////////
    // HEXtoRGB @v1.0
    NioApp.hexRGB = function (hex, op) {
        var color, colorRGB; var opc = (op) ? op : 1;
        hex = hex.replace(/\s/g, '');
        if (/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)) {
            color = hex.substring(1).split('');
            if (color.length === 3) {
                color = [color[0], color[0], color[1], color[1], color[2], color[2]];
            }
            color = '0x' + color.join('');
            colorRGB = [(color >> 16) & 255, (color >> 8) & 255, color & 255].join(',');
            return (opc >= 1) ? 'rgba(' + colorRGB + ')' : 'rgba(' + colorRGB + ',' + opc + ')';
        }
        throw new Error('bad hex');
    }

    //Time Converter @v1.0
    NioApp.to12 = function(time) {
        time = time.toString ().match (/^([01]\d|2[0-3])(:)([0-5]\d)(:[0-5]\d)?$/) || [time];
        if (time.length > 1) { 
            time = time.slice (1);
            time.pop();
            time[5] = +time[0] < 12 ? ' AM' : ' PM'; // Set AM/PM
            time[0] = +time[0] % 12 || 12;
        }
        time = time.join ('');
        return time;
    }

    // attribute maker
    NioApp.attr = function(selector,property,value) {
        const att = document.createAttribute(property);
            att.value = value;
            selector.setAttributeNode(att);
    }
    
    //slide up
    NioApp.SlideUp = function (target, duration=500) {
        target.style.transitionProperty = 'height, margin, padding';
        target.style.transitionDuration = duration + 'ms';
        target.style.boxSizing = 'border-box';
        target.style.height = target.offsetHeight + 'px';
        target.offsetHeight; target.style.overflow = 'hidden'; target.style.height = 0;
        target.style.paddingTop = 0; target.style.paddingBottom = 0;
        target.style.marginTop = 0; target.style.marginBottom = 0;
        window.setTimeout( () => {
            target.style.display = 'none';
            target.style.removeProperty('height');
            target.style.removeProperty('padding-top');
            target.style.removeProperty('padding-bottom');
            target.style.removeProperty('margin-top');
            target.style.removeProperty('margin-bottom');
            target.style.removeProperty('overflow');
            target.style.removeProperty('transition-duration');
            target.style.removeProperty('transition-property');
        }, duration);
    };

    //side down
    NioApp.SlideDown = function (target, duration=500) {
        target.style.removeProperty('display');
        let display = window.getComputedStyle(target).display;
        if (display === 'none') display = 'block';
        target.style.display = display;
        let height = target.offsetHeight; 
        target.style.overflow = 'hidden'; target.style.height = 0; target.style.paddingTop = 0;
        target.style.paddingBottom = 0; target.style.marginTop = 0;
        target.style.marginBottom = 0; target.offsetHeight;
        target.style.boxSizing = 'border-box';
        target.style.transitionProperty = "height, margin, padding";
        target.style.transitionDuration = duration + 'ms';
        target.style.height = height + 'px';
        target.style.removeProperty('padding-top'); target.style.removeProperty('padding-bottom');
        target.style.removeProperty('margin-top'); target.style.removeProperty('margin-bottom');
        window.setTimeout( () => {
          target.style.removeProperty('height');
          target.style.removeProperty('overflow');
          target.style.removeProperty('transition-duration');
          target.style.removeProperty('transition-property');
        }, duration);
    };

    //slide toggle
    NioApp.SlideToggle = function (target, duration=500) {
        if (window.getComputedStyle(target).display === 'none') {
            return NioApp.SlideDown(target, duration);
          } else {
            return NioApp.SlideUp(target, duration);
        }
    };

    // to min
    NioApp.toMin = function(input) {
        let value = input.split(':');
        let hour = parseInt(value[0]);
        let min = (value[1] !== undefined) ? parseInt(value[1]) : 0;
        let time = (hour*60) + min
        return time;
    }

    //to time
    NioApp.toTime = function(totalMinutes) {
        const minutes = totalMinutes % 60;
        const hours = Math.floor(totalMinutes / 60);
        return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
    }

    //generate random text
    NioApp.randomId = function(length) {
        var result           = '';
        var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        var charactersLength = characters.length;
        for ( var i = 0; i < length; i++ ) {
          result += characters.charAt(Math.floor(Math.random() * 
     charactersLength));
       }
       return result;
    }

    //get parent Elements
    NioApp.getParents = function(el, selector, filter) {
        // If no selector defined will bubble up all the way to *document*
        let parentSelector = (selector === undefined) ? document : document.querySelector(selector);
        var parents = [];
        var pNode = el.parentNode;
        
        while (pNode !== parentSelector) {
            var element = pNode;

            if(filter === undefined){
                parents.push(element); // Push that parentSelector you wanted to stop at
            }else{
                element.classList.contains(filter) && parents.push(element);
            }
            pNode = element.parentNode;
        }
        
        return parents;
    }

    //Extend Object
    NioApp.extendObject = function(obj, ext) {
        Object.keys(ext).forEach(function (key) { obj[key] = ext[key]; });
        return obj;
    }

    //Toggle
    NioApp.Toggle = {
        class: function(selector, selfActive = 'active', targetActive = 'active', parentActive = 'active'){
            let elm = document.querySelectorAll(selector);
            let overlayClass = 'nk-overlay';
            elm.forEach(item => {
                let target = item.dataset.target;
                let parent = item.dataset.parent;
                let targetElm = document.getElementById(target);
                let thisBreak = targetElm.dataset.break;
                let thisVisiable = targetElm.dataset.visiable;
                let parentElm = document.getElementById(parent);
                let trigger = document.querySelectorAll(`[data-target="${target}"]`);
                item.addEventListener('click', function(e){
                    e.preventDefault();
                    //trigger Toggle
                    trigger.forEach(elm =>{
                        !elm.classList.contains(selfActive) ? elm.classList.add(selfActive) : elm.classList.remove(selfActive);
                    });
                    //target Toggle
                    !targetElm.classList.contains(targetActive) ? targetElm.classList.add(targetActive) : targetElm.classList.remove(targetActive);

                    //overlay (if true)
                    let overlay = targetElm.dataset.overlay ? targetElm.dataset.overlay : false;

                    if(overlay){
                        let overlayTemplate = `${parent ? `<div class="${overlayClass}" data-target="${target}" data-parent="${parent}"></div>` : `<div class="${overlayClass}" data-target="${target}"></div>`}`;
                        if(targetElm.classList.contains(targetActive) && (thisVisiable ? window.innerWidth < eval(`NioApp.Break.${thisVisiable}`) : true) ){
                            targetElm.insertAdjacentHTML('beforebegin', overlayTemplate);
                        }else if(window.innerWidth < eval(`NioApp.Break.${thisVisiable}`)){
                           let thisOverlay = document.querySelector(`.${overlayClass}[data-target="${target}"]`);
                           thisOverlay.remove();
                        }
                    }
                    //parent Toggle (if defined)
                    if(parent){
                        !parentElm.classList.contains(parentActive) ? parentElm.classList.add(parentActive) : parentElm.classList.remove(parentActive);
                    }
                    //close overlay click
                    if(overlay && targetElm.classList.contains(targetActive) && (thisVisiable ? window.innerWidth < eval(`NioApp.Break.${thisVisiable}`) : true)){
                        let thisOverlay = document.querySelector(`.${overlayClass}[data-target="${target}"]`);
                        thisOverlay.addEventListener('click', function(e){
                            e.preventDefault();
                            //trigger Toggle
                            trigger.forEach(elm =>{
                                elm.classList.remove(selfActive);
                            });
                            targetElm.classList.remove(targetActive);
                            parent && parentElm.classList.remove(parentActive);
                            thisOverlay.remove();
                        })
                    }
                    // if(window.innerWidth < eval(`NioApp.Break.${thisBreak}`)){
                    //     targetElm.classList.add('toggle-collapsed');
                    // }
                    console.log(window.innerWidth > eval(`NioApp.Break.${thisVisiable}`));
                    if(window.innerWidth > eval(`NioApp.Break.${thisVisiable}`)){
                        targetElm.classList.contains(targetActive) ? targetElm.classList.add('toggle-visiable') : targetElm.classList.remove('toggle-visiable');
                    }
                })

                if(window.innerWidth <= eval(`NioApp.Break.${thisBreak}`)){
                    trigger.forEach(elm =>{
                        elm.classList.remove(selfActive);
                    });
                    let thisOverlay = document.querySelector(`.${overlayClass}[data-target="${target}"]`);
                    thisOverlay && thisOverlay.remove();
                    targetElm.classList.remove(targetActive);
                    parent && parentElm.classList.remove(parentActive);
                    targetElm.classList.add('toggle-collapsed');
                }

                //resize event 
                window.addEventListener('resize', function(){

                    if(window.innerWidth <= eval(`NioApp.Break.${thisBreak}`)){
                        setTimeout(() => {
                            targetElm.classList.add('toggle-collapsed');
                        }, 500);
                    }else{
                        targetElm.classList.remove('toggle-collapsed');
                    }
                    if(window.innerWidth >= eval(`NioApp.Break.${thisBreak}`)){
                        let thisOverlay = document.querySelector(`.${overlayClass}[data-target="${target}"]`);
                        thisOverlay && thisOverlay.remove();
                        if(!thisVisiable){
                            trigger.forEach(elm =>{
                                elm.classList.remove(selfActive);
                            });
                            targetElm.classList.remove(targetActive);
                            parent && parentElm.classList.remove(parentActive);
                        }
                    }else{
                        if(thisVisiable && targetElm.classList.contains('toggle-visiable') && !targetElm.classList.contains('toggle-collapsed')){
                            trigger.forEach(elm =>{
                                elm.classList.remove(selfActive);
                            });
                            let thisOverlay = document.querySelector(`.${overlayClass}[data-target="${target}"]`);
                            thisOverlay && thisOverlay.remove();
                            targetElm.classList.remove(targetActive);
                            parent && parentElm.classList.remove(parentActive);
                        }
                    }

                })
            });
        }
    }

    ///////////////////////////////
    //TimePicker 1.0
    /////////////////////////////
    NioApp.Custom.timePicker = function (selector,opt) {

        let options = {
            format: opt.format ? opt.format : 24,
            interval : opt.interval ? opt.interval : 30,
            start : opt.start ? opt.start : '00:00',
            end : opt.end ? opt.end : '23:59',
            class: {
                dropdown: 'nk-timepicker-dropdown',
                dropdownItem: 'nk-timepicker-time',
            }
        }

        let timeInterval = options.interval;
        let timeFormat = options.format;
        let timeStart = options.start;
        let timeEnd = options.end;
        let totalTime = NioApp.toMin(timeEnd) - NioApp.toMin(timeStart);
        let timeSlot = Math.floor(totalTime / timeInterval);
        let items = []

        let startTime = NioApp.toMin(timeStart);
      
        for (let i = 0; i < timeSlot+1; i++) {
            let currentTime = startTime;
            let timeString = function(){
                if(timeFormat == 12){
                    return NioApp.to12(NioApp.toTime(currentTime));
                }else{
                    return NioApp.toTime(currentTime)
                }
            };
            items.push(`<li><button class="dropdown-item ${options.class.dropdownItem}" data-picker-text="${timeString()}" type="button">
                ${timeString()}
            </button></li>`
            )
            startTime = currentTime + timeInterval;
        }

        let itemsMarkups = items.join('');
        NioApp.attr(selector,'data-bs-toggle','dropdown');
        NioApp.attr(selector,'data-bs-offset','0,5');

        let id = selector.id ? selector.id : NioApp.randomId(8);
        
        if(!selector.id){
            NioApp.attr(selector,'id',id);
        }

        let dropdownTemplate = `
        <ul class="dropdown-menu ${options.class.dropdown}" data-picker-id="${id}" style="max-height:320px;overflow:auto">
            ${itemsMarkups}
        </ul>
        `
        selector.insertAdjacentHTML('afterend', dropdownTemplate);

        let timeSelector = document.querySelectorAll(`.${options.class.dropdownItem}`);
        timeSelector.forEach(item => {
            item.addEventListener("click", function(e){
                e.preventDefault();
                let itemtext = item.dataset.pickerText;
                let input = document.getElementById(item.closest(`.${options.class.dropdown}`).dataset.pickerId);
                input.value = itemtext;
                //set active slot
                let allItems = item.closest(`.${options.class.dropdown}`).querySelectorAll(`.${options.class.dropdownItem}`);
                allItems.forEach(otherItem=>{
                    otherItem.classList.remove('active');
                })
                item.classList.add('active');
              });
        })
    }


    //Bootstrap
    NioApp.BS.tooltip = function (selector) {
        let tooltipEl = document.querySelectorAll(selector);
        let tooltipTriggerList = [].slice.call(tooltipEl);
        let tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl)
        })
    };

    NioApp.BS.popover = function (selector) {
        const popoverTriggerList = [].slice.call(document.querySelectorAll(selector));
        if(popoverTriggerList !== null){
            const popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
                return new bootstrap.Popover(popoverTriggerEl)
            });
        }
    };

    NioApp.BS.toast = function(selector) {
        const toastTrigger = document.querySelectorAll(selector);
        if(toastTrigger.length > 0){
            toastTrigger.forEach(item => {
                let target = item.dataset.bsTarget;
                const toastLive = document.getElementById(target);
                item.addEventListener('click', function () {
                    const toast = new bootstrap.Toast(toastLive);
                    toast.show()
                })
            })
        }
    };

    NioApp.BS.alertTemplate = function(selector, message, variant) {
        const target = document.getElementById(selector)
        const wrapper = document.createElement('div')
        wrapper.innerHTML = `<div class="alert alert-${variant ? variant : 'primary'} alert-dismissible" role="alert">
                <div>${message}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>`
        target.append(wrapper)
    };

    NioApp.BS.alert = function(selector, options) {
        const alertTrigger = document.querySelectorAll(selector);
        if(alertTrigger.length > 0){
            alertTrigger.forEach(item => {
                const target = item.dataset.bsTarget ? item.dataset.bsTarget : options.target;
                const variant = item.dataset.bsVariant ? item.dataset.bsVariant : options.variant;
                const content = item.dataset.bsContent ? item.dataset.bsContent : options.content; 
                item.addEventListener('click', function () {
                    NioApp.BS.alertTemplate(target, content, variant)
                })
            })
        }
    }

    NioApp.BS.validate = function(selector) {
        const forms = document.querySelectorAll(selector);
        Array.from(forms).forEach(form => {
            form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault()
                event.stopPropagation()
            }
            form.classList.add('was-validated')
            }, false)
        })
    }

    ///////////////////////////////
    // Initial by default
    /////////////////////////////
    NioApp.onResize(NioApp.StateUpdate);

    return NioApp;
}(NioApp);