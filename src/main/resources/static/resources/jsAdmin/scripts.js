!(function (NioApp) {
"use strict";


/*  =======================================================
  Custom Menu (sidebar/header)
========================================================== */
let menu={
  classes: {
    main: 'nk-menu',
    item:'nk-menu-item',
    link:'nk-menu-link',
    toggle: 'nk-menu-toggle',
    sub: 'nk-menu-sub',
    subparent: 'has-sub',
    active: 'active',
    current: 'current-page'
  },
};

let nav={
  classes: {
    main: 'nk-nav',
    item:'nk-nav-item',
    link:'nk-nav-link',
    toggle: 'nk-nav-toggle',
    sub: 'nk-nav-sub',
    subparent: 'has-sub',
    active: 'active',
    current: 'current-page'
  },
};

NioApp.Dropdown = {
  load: function(elm,subparent){
    let parent = elm.parentElement;
    if(!parent.classList.contains(subparent)){
      parent.classList.add(subparent);
    }
  },
  toggle: function(elm,active){

    let parent = elm.parentElement;
    let nextelm = elm.nextElementSibling;
    let speed = nextelm.children.length > 5 ? 400 + nextelm.children.length * 10 : 400;
    if(!parent.classList.contains(active)){
      parent.classList.add(active);
      NioApp.SlideDown(nextelm,speed);
    }else{
      parent.classList.remove(active);
      NioApp.SlideUp(nextelm,speed);
    }
  },
  extended: function(elm,active){
    let parent = elm.parentElement;
    let nextelm = elm.nextElementSibling;
    let navbarCollapse = NioApp.body.dataset.navbarCollapse ? NioApp.body.dataset.navbarCollapse : NioApp.Break.lg;
    if(NioApp.Win.width > eval(`NioApp.Break.${navbarCollapse}`)){
      
      elm.addEventListener('mouseenter', (event) => {
        
        let placement = NioApp.getParents(elm,`.${nav.classes.main}`,nav.classes.sub).length > 0 ? 'right-start' : 'bottom-start';
        Popper.createPopper(elm, nextelm, {
          placement: placement,
          boundary: '.nk-wrap',
        });
      });
    }
  },
  closeSiblings: function(elm,active,subparent,submenu){
    let parent = elm.parentElement;
    let siblings = parent.parentElement.children;
    Array.from(siblings).forEach(item => {
      if(item !== parent){
        item.classList.remove(active);
        if(item.classList.contains(subparent)){
          let subitem = item.querySelectorAll(`.${submenu}`);
          subitem.forEach(child => {
            child.parentElement.classList.remove(active);
            NioApp.SlideUp(child,400);
          })
        }
      }
    });
  }
}
//init dropdown
NioApp.Dropdown.sidebar = function (selector){
  const elm = document.querySelectorAll(selector);
  let active = menu.classes.active;
  let subparent = menu.classes.subparent;
  let submenu = menu.classes.sub;
  elm.forEach(item => {
    NioApp.Dropdown.load(item,subparent);
    item.addEventListener("click", function(e){
      e.preventDefault();
      NioApp.Dropdown.toggle(item,active);
      NioApp.Dropdown.closeSiblings(item,active,subparent,submenu);
    });
  })
};

NioApp.Dropdown.header = function (selector){
  const elm = document.querySelectorAll(selector);
  let active = nav.classes.active;
  let subparent = nav.classes.subparent;
  let submenu = nav.classes.sub;
  let navbarCollapse = NioApp.body.dataset.navbarCollapse ? NioApp.body.dataset.navbarCollapse : NioApp.Break.lg;
  elm.forEach(item => {
    NioApp.Dropdown.load(item,subparent);
    NioApp.Dropdown.extended(item);
    item.addEventListener("click", function(e){
      e.preventDefault();
      if(NioApp.Win.width < eval(`NioApp.Break.${navbarCollapse}`)){
        NioApp.Dropdown.toggle(item,active);
        NioApp.Dropdown.closeSiblings(item,active,subparent,submenu);
      }
    });
  })
}


/*  =======================================================
  Custom Sidebar
========================================================== */
let sidebar = {
  classes:{
    base: 'nk-sidebar',
    toggle: 'sidebar-toggle',
    toggleActive: 'active',
    active: 'sidebar-active',
    overlay: 'sidebar-overlay',
    body: 'sidebar-shown',
    compact: 'is-compact',
    compactToggle: 'compact-toggle',
    compactToggleActive: 'active',
    compactBody:'sidebar-compact'
  },
  break: {
    main: NioApp.body.dataset.sidebarCollapse ? eval(`NioApp.Break.${NioApp.body.dataset.sidebarCollapse}`) : NioApp.Break.lg,
  }
};


NioApp.Sidebar = {
  show: function(toggle,target) {
      toggle.forEach(toggleItem => {
        toggleItem.classList.add(sidebar.classes.toggleActive);
      })
      target.classList.add(sidebar.classes.active);
      NioApp.body.classList.add(sidebar.classes.body);
      let overalyTemplate = `<div class='${sidebar.classes.overlay}'></div>`
      target.insertAdjacentHTML('beforebegin', overalyTemplate);
  },
  hide: function(toggle,target) {
      toggle.forEach(toggleItem => {
        toggleItem.classList.remove(sidebar.classes.toggleActive);
      })
      target.classList.remove(sidebar.classes.active);
      NioApp.body.classList.remove(sidebar.classes.body);
      let overlay = document.querySelector(`.${sidebar.classes.overlay}`);
      setTimeout(() => {
        overlay && overlay.remove();
      }, 300);
  },
  compact: function(toggle,target) {
    toggle.classList.toggle(sidebar.classes.compactToggleActive);
    target.classList.toggle(sidebar.classes.compact);
    NioApp.body.classList.toggle(sidebar.classes.compactBody);
  }
}

NioApp.Sidebar.init = function() {
  let targetSl = document.querySelector(`.${sidebar.classes.base}`);
  let toggleSl = document.querySelectorAll(`.${sidebar.classes.toggle}`);
  let compactSl = document.querySelector(`.${sidebar.classes.compactToggle}`);
  toggleSl.forEach(item => {
    item.addEventListener("click", function(e){
      e.preventDefault();
      if(sidebar.break.main > NioApp.Win.width){
        if(!targetSl.classList.contains(sidebar.classes.active)){
            NioApp.Sidebar.show(toggleSl,targetSl);
        }else{
            NioApp.Sidebar.hide(toggleSl,targetSl);
        }
      }
    });

    window.addEventListener("resize", function(e){
      if(sidebar.break.main < NioApp.Win.width){
        NioApp.Sidebar.hide(toggleSl,targetSl);
      }
    });

    document.addEventListener("mouseup", function(e){
      if(e.target.closest(`.${sidebar.classes.base}`) === null){
        NioApp.Sidebar.hide(toggleSl,targetSl);
      }
    });
    
  })
  //init Compact Mode
  if(compactSl){
    compactSl.addEventListener("click", function(e){
      e.preventDefault();
      if(sidebar.break.main < NioApp.Win.width){
        NioApp.Sidebar.compact(compactSl,targetSl);
      }
    });
  }
}

/*  =======================================================
  Custom NavBar
========================================================== */
let navbar = {
  classes:{
    base: 'nk-navbar',
    toggle: 'navbar-toggle',
    toggleActive: 'active',
    active: 'navbar-active',
    overlay: 'navbar-overlay',
    body: 'navbar-shown',
  },
  break: {
    main: NioApp.body.dataset.navbarCollapse ? eval(`NioApp.Break.${NioApp.body.dataset.navbarCollapse}`) : NioApp.Break.lg,
  }
};
NioApp.Navbar = {
  show: function(toggle,target) {
      toggle.forEach(toggleItem => {
        toggleItem.classList.add(navbar.classes.toggleActive);
      })
      target.classList.add(navbar.classes.active);
      NioApp.body.classList.add(navbar.classes.body);
      let overalyTemplate = `<div class='${navbar.classes.overlay}'></div>`
      target.insertAdjacentHTML('beforebegin', overalyTemplate);
  },
  hide: function(toggle,target) {
      toggle.forEach(toggleItem => {
        toggleItem.classList.remove(navbar.classes.toggleActive);
      })
      target.classList.remove(navbar.classes.active);
      NioApp.body.classList.remove(navbar.classes.body);
      let overlay = document.querySelector(`.${navbar.classes.overlay}`);
      setTimeout(() => {
        overlay && overlay.remove();
      }, 300);
  },
  mobile: function(target){
    if(navbar.break.main < NioApp.Win.width){
      target.classList.remove('navbar-mobile');
    }else{
      setTimeout(() => {
        target.classList.add('navbar-mobile');
      }, 500);
    }
  }
}

NioApp.Navbar.init = function() {
  let targetSl = document.querySelector(`.${navbar.classes.base}`);
  let toggleSl = document.querySelectorAll(`.${navbar.classes.toggle}`);
  toggleSl.forEach(item => {
    NioApp.Navbar.mobile(targetSl);
    item.addEventListener("click", function(e){
      e.preventDefault();
      if(navbar.break.main > NioApp.Win.width){
        if(!targetSl.classList.contains(navbar.classes.active)){
            NioApp.Navbar.show(toggleSl,targetSl);
        }else{
            NioApp.Navbar.hide(toggleSl,targetSl);
        }
      }
    });

    window.addEventListener("resize", function(e){
      if(navbar.break.main < NioApp.Win.width){
        NioApp.Navbar.hide(toggleSl,targetSl);
      }
      NioApp.Navbar.mobile(targetSl);
    });

    document.addEventListener("mouseup", function(e){
      if(e.target.closest(`.${navbar.classes.base}`) === null){
        NioApp.Navbar.hide(toggleSl,targetSl);
      }
    });
  })
}
/*  =======================================================
  Add some class to current link
========================================================== */
NioApp.CurrentLink = function(selector, parent, submenu, base, active, intoView){
  let elm = document.querySelectorAll(selector);
  let currentURL = document.location.href,
      removeHash = currentURL.substring(0, (currentURL.indexOf("#") == -1) ? currentURL.length : currentURL.indexOf("#")),
      removeQuery = removeHash.substring(0, (removeHash.indexOf("?") == -1) ? removeHash.length : removeHash.indexOf("?")),
      fileName = removeQuery;
  elm.forEach(function(item){
    var selfLink = item.getAttribute('href');
    if (fileName.match(selfLink)) {
      let parents = NioApp.getParents(item,`.${base}`, parent);
      parents.forEach(parentElemets =>{
        parentElemets.classList.add(...active);
        let subItem = parentElemets.querySelector(`.${submenu}`);
        subItem !== null && (subItem.style.display = "block")
      })
      intoView && item.scrollIntoView({ block: "end"})
    } else {
      item.parentElement.classList.remove(...active);
    }
  })
}

/*  =======================================================
  Clipboard js
========================================================== */
NioApp.Clipboard = function(selector) {
  let clipboardTrigger = document.querySelectorAll(selector);
  let options = {
    tooltip:{
      selector: `${selector.slice(1)}-tooltip`,
      init: 'Copy',
      success : 'Copied',
    },
    icon:{
      init: 'copy',
      success: 'copy-fill',
    }
  }
  clipboardTrigger.forEach(item => {
    //init clipboard
    let clipboard = new ClipboardJS(item);
    //set markup
    let initMarkup = `<em class="icon ni ni-${options.icon.init}"></em><span class="${options.tooltip.selector}">${options.tooltip.init}</span>`;
    let successMarkup = `<em class="icon ni ni-${options.icon.success}"></em><span class="${options.tooltip.selector}">${options.tooltip.success}</span>`;
    item.innerHTML = initMarkup;
    //on-sucess
    clipboard.on("success", function(e){
      let target = e.trigger;
      target.innerHTML = successMarkup;
      setTimeout(function(){
        target.innerHTML = initMarkup;
      }, 1000)
    });
  });
}

/*  ================================================================
  Custom select js (Choices)
==================================================================== */
NioApp.Select = function(selector,options){
  let elm = document.querySelectorAll(selector);
  if( elm.length > 0 ){
    elm.forEach(item => {
      let search = item.dataset.search ? JSON.parse(item.dataset.search) : false;
      let sort = item.dataset.sort ? JSON.parse(item.dataset.sort) : false;
      let cross = item.dataset.cross ? JSON.parse(item.dataset.cross) : true;
      const choices = new Choices(item, {
        silent: true,
        allowHTML: false,
        searchEnabled: search,
        placeholder: true,
        placeholderValue: null,
        searchPlaceholderValue: 'Search',
        shouldSort: sort,
        removeItemButton: cross,
      });
    })
  }
}

/*  ================================================================
  Custom tags js (Choices)
==================================================================== */
NioApp.Tags = function(selector){
  let elm = document.querySelectorAll(selector);
  if( elm.length > 0 ){
    elm.forEach(item => {
      let cross = item.dataset.cross ? JSON.parse(item.dataset.cross) : true;
      let plainText = item.classList.contains('form-control-plaintext') && `choices__inner-plaintext`;
      let containerInner = `choices__inner ${plainText && plainText}`
      const choices = new Choices(item, {
        silent: true,
        allowHTML: false,
        placeholder: true,
        placeholderValue: null,
        removeItemButton: cross,
        classNames: {
          containerInner: containerInner,
        }
      });
    })
  }
}

/*  ================================================================
  TimePicker (Custom)
==================================================================== */
NioApp.TimePicker = function(selector,opt){
  let elm = document.querySelectorAll(selector);
  if( elm.length > 0 ){
    elm.forEach(item => {
      let format = item.dataset.format ? parseInt(item.dataset.format) : 12;
      let interval = item.dataset.interval ? parseInt(item.dataset.interval) : 30;
      let startTime = item.dataset.startTime ? item.dataset.startTime : '12:00';
      let endTime = item.dataset.endTime ? item.dataset.endTime : '23:00';
      let def = {
        format: format,
        interval : interval,
        start : startTime,
        end : endTime
      }, 
      attr = (opt) ? NioApp.extendObject(def, opt) : def;
      NioApp.Custom.timePicker(item,attr)
    })
  }
}
/*  ================================================================
  DatePicker (vanillajs-datepicker)
==================================================================== */
NioApp.DatePicker = function(selector,opt){
  let elm = document.querySelectorAll(selector);
  if( elm.length > 0 ){
    elm.forEach(item => {
      let autohide = item.dataset.autoHide ? JSON.parse(item.dataset.autoHide) : true;
      let clearBtn = item.dataset.clearBtn ? JSON.parse(item.dataset.clearBtn) : false;
      let format = item.dataset.format ? item.dataset.format : 'mm/dd/yyyy';
      let maxView = item.dataset.maxView ? parseInt(item.dataset.maxView) : 3;
      let pickLevel = item.dataset.pickLevel ? parseInt(item.dataset.pickLevel) : 0;
      let startView = item.dataset.startView ? parseInt(item.dataset.startView) : 0;
      let title = item.dataset.title ? item.dataset.title : '';
      let todayBtn = item.dataset.todayBtn ? JSON.parse(item.dataset.todayBtn) : false;
      let todayBtnMode = item.dataset.todayBtnMode ? parseInt(item.dataset.todayBtnMode) : 0;
      let weekStart = item.dataset.weekStart ? parseInt(item.dataset.weekStart) : 0;
      let rangePicker = item.dataset.range ? true : false;
      let def = {
        buttonClass: 'btn btn-md',
        autohide: autohide,
        clearBtn: clearBtn,
        format: format,
        maxView: maxView,
        pickLevel: pickLevel,
        startView: startView,
        title: title,
        todayBtn: todayBtn,
        todayBtnMode: todayBtnMode,
        weekStart: weekStart
      },
      attr = opt ? opt : def;
      const datepicker = rangePicker ? new DateRangePicker(item, attr) : new Datepicker(item, attr);
    })
  }
}

/*  ================================================================
  Pureknob
==================================================================== */
NioApp.Pureknob = function(selector) {
  let elm = document.querySelectorAll(selector);
  elm.forEach(item => {
      let size = item.dataset.size ? parseInt(item.dataset.size) : 100;
      let angleStart = item.dataset.angleStart ? item.dataset.angleStart : 1;
      let angleEnd = item.dataset.angleEnd ? item.dataset.angleEnd : 1;
      let angleOffset = item.dataset.angleOffset ? item.dataset.angleOffset : false;
      let colorBg = item.dataset.colorBg ? item.dataset.colorBg : NioApp.Colors.gray200;
      let colorFg = item.dataset.colorFg ? item.dataset.colorFg : NioApp.Colors.primary;
      let trackWidth = item.dataset.trackWidth ? item.dataset.trackWidth : .2;
      let min = item.dataset.min ? item.dataset.min : 0;
      let max = item.dataset.max ? item.dataset.max : 100;
      let readonly = item.dataset.readonly ? JSON.parse(item.dataset.readonly) : true;
      let value = item.dataset.value ? parseInt(item.dataset.value) : 0;
      let hideValue = item.dataset.hideValue ? 0 : 1;

      // Create knob element, by given value size.
      let knob = pureknob.createKnob(size,size);

      // Set properties.
      knob.setProperty('angleStart', -angleStart * Math.PI);
      knob.setProperty('angleEnd', angleEnd * Math.PI);
      knob.setProperty('angleOffset', angleOffset * Math.PI);
      knob.setProperty('colorFG', colorFg);
      knob.setProperty('colorBG', colorBg);
      knob.setProperty('trackWidth', trackWidth);
      knob.setProperty('valMin', min);
      knob.setProperty('valMax', max);
      knob.setProperty('readonly', readonly);
      knob.setProperty('textScale', hideValue);

      // Set initial value.
      knob.setValue(value);

      // Create element node.
      let node = knob.node();

      // Add it to the DOM.
      item.appendChild(node);
  });
}

/*  ================================================================
  Drag and drop input/upload field (dropzone)
==================================================================== */
NioApp.Dropzone = function(selector){
  let elm = document.querySelectorAll(selector);
  if( elm != 'undefined' && elm != null ){
    elm.forEach(item => {
      let itemId = item.id;
      let maxFiles = item.dataset.maxFiles ? parseInt(item.dataset.maxFiles) : null;
      let maxFilesize = item.dataset.maxFilesize ? parseInt(item.dataset.maxFilesize) : 256;
      let messageIcon = item.dataset.messageIcon ? item.dataset.messageIcon : ((maxFiles === 1) ? 'file' : 'files');
      let acceptedFiles = item.dataset.acceptedFiles ? item.dataset.acceptedFiles : null;

      //add styling Class 
      item.classList.add('dropzone');
      let dzFileSize = item.querySelector('.dz-message-text');
      item.dataset.maxFilesize && dzFileSize.insertAdjacentHTML('beforeend', `<small>Max ${maxFilesize} MiB</small>`);
      //filesize
      
      //icon
      let dzIcon = item.querySelector('.dz-message-icon:empty');
      let dzIconMarkUp = `<em class="icon icon-lg ni ni-${messageIcon}"></em>`
      dzIcon ? dzIcon.innerHTML = dzIconMarkUp : null;
      let myDropzone = new Dropzone(`#${itemId}`,{
        url: "image",
        maxFilesize: maxFilesize,
        maxFiles: maxFiles,
        acceptedFiles: acceptedFiles
      });
    })
  }
}

/*  ================================================================
  Range Input (noUiSlider)
==================================================================== */
NioApp.Range = function(selector,opt){
  let elm = document.querySelectorAll(selector);
  if( elm != 'undefined' && elm != null ){
    elm.forEach(item => {
      let itemId = item.id;

      let start = item.dataset.start
          start = /\s/g.test(start) ? start.split(' ') : start
          start = start ? start : 0;
      let connect = item.dataset.connect;
          connect = /\s/g.test(connect) ? connect.split(' ') : connect;
          connect = typeof connect == 'undefined' ? 'lower' : connect;
          connect = connect == 'true' || connect == 'false' ? JSON.parse(connect) : connect;
      let min = item.dataset.min ? parseInt(item.dataset.min) : 0;
      let max = item.dataset.max ? parseInt(item.dataset.max) : 100;
      let minDistance = item.dataset.minDistance ? parseInt(item.dataset.minDistance) : null;
      let maxDistance = item.dataset.maxDistance ? parseInt(item.dataset.maxDistance) : null;
      let step = item.dataset.step ? parseInt(item.dataset.step) : 1;
      let orientation = item.dataset.orientation ? item.dataset.orientation : 'horizontal';
      let tooltip = item.dataset.tooltip ? JSON.parse(item.dataset.tooltip) : false;
      console.log(start,connect);
      var def = {
        start: start,
        connect: connect,
        direction: NioApp.State.isRTL ? "rtl" : "ltr",
        range: {
            'min': min,
            'max': max
        },
        margin: minDistance,
        limit: maxDistance,
        step: step,
        orientation: orientation,
        tooltips: tooltip
      },
      attr = (opt) ? NioApp.extendObject(def, opt) : def;
      noUiSlider.create(item, attr);
    })
  }
}

/*  ================================================================
  Form Validation (pristine.js)
==================================================================== */
NioApp.FormValidate = function(selector){
  let elm = document.querySelectorAll(selector);
  if( elm != 'undefined' && elm != null ){
    elm.forEach(item => {
      let itemId = item.id;
      var form = document.getElementById(itemId);

      var pristine = new Pristine(form, {
          classTo: 'form-group',
          errorClass: 'is-invalid',
          successClass: 'is-valid',
          errorTextParent: 'form-control-wrap',
          errorTextTag: 'div',
          errorTextClass: 'form-error' 
      });

      form.addEventListener('submit', function (e) {
          e.preventDefault();
          var valid = pristine.validate();
          form.classList.add('validated');
      });
    })
  }
}


/*  =======================================================
  Third party Addons init 
========================================================== */
NioApp.Addons.init = function () {
  NioApp.Clipboard('.js-copy');
  NioApp.Select('.js-select');
  NioApp.Tags('.js-tags');
  NioApp.DatePicker('.js-datepicker');
  NioApp.TimePicker('.js-timepicker');
  NioApp.Pureknob('.js-pureknob');
  NioApp.Dropzone('.js-upload');
  NioApp.Range('.js-range');
  NioApp.FormValidate('.js-validate');
}

/*  =======================================================
  Set Progressbar Width
========================================================== */
NioApp.Custom.progress = function(){
  let progressBar = document.querySelectorAll('[data-progress]');
  progressBar.forEach(item => {
    let amount = item.dataset.progress;
    item.style.width = amount;
  })
}

/*  =======================================================
  Show hide pasword
========================================================== */
NioApp.Custom.showHidePassword = function(selector){
  let elem = document.querySelectorAll(selector);
  if(elem.length > 0){
    elem.forEach(item => {
      item.addEventListener("click", function(e){
        e.preventDefault();
        let target = document.getElementById(item.getAttribute("href"));
        if(target.type == "password") {
          target.type = "text";
          item.classList.add("is-shown");
        }else{
          target.type = "password";
          item.classList.remove("is-shown");
        }
      });

    });
  }
}

/*  =======================================================
  Upload image
========================================================== */
NioApp.Custom.uploadImage = function(selector) {
  let elem = document.querySelectorAll(selector);
  if(elem.length > 0) {
    elem.forEach(item => {
      item.addEventListener("change", function(){
        if(item.files && item.files[0]) {
          let img = document.getElementById(item.dataset.target);
          img.onload = function() {
            URL.revokeObjectURL(img.src);
          }
          img.src = URL.createObjectURL(item.files[0]);

          let allowedExtensions  = ["jpg", "JPEG", "JPG", "png" ];
          let fileExtension  = this.value.split(".").pop();
          var lastDot = this.value.lastIndexOf('.');
          var ext = this.value.substring(lastDot + 1);
          var extTxt = img.value = ext;
          if (!allowedExtensions.includes(fileExtension)) {
              alert(extTxt + " file type not allowed, Please upload jpg, JPG, JPEG, or png file");
              img.src = " ";
          }
        }

      })
    });
  }
}
/*  ================================================================
  Set background image
==================================================================== */
NioApp.Custom.setBg = function() {
  let elem = document.querySelectorAll('[data-bg]');
  elem.forEach(item => {
    let src = item.dataset.bg;
    item.style.backgroundImage="url('" + src + "')";
  })
}
/*  ================================================
  Todo checkboxes
===================================================== */
NioApp.Custom.todoCheckboxToggle = function(selector) {
  let elem = document.querySelectorAll(selector);

  if(elem) {
    elem.forEach(item => {
      item.addEventListener("change", function(e){
        let parentElem = NioApp.getParents(item, '.nk-todo-list', 'nk-todo-item');
        
        if(e.target.checked) {
          parentElem.forEach(item => {
            item.classList.add('task-done');
          })
        } else {
          parentElem.forEach(item => {
            item.classList.remove('task-done');
          })
        }
      })
    })
  }
}

/*  =======================================================
  Custom Scripts init 
========================================================== */
NioApp.Custom.init = function () {
  NioApp.Custom.progress();
  NioApp.Custom.showHidePassword(".password-toggle");
  NioApp.Custom.uploadImage(".upload-image");
  NioApp.Sidebar.init();
  NioApp.Navbar.init();
  NioApp.CurrentLink(`.${menu.classes.link}`, menu.classes.item, menu.classes.sub, menu.classes.main,[menu.classes.active, menu.classes.current],true);
  NioApp.Dropdown.sidebar(`.${menu.classes.toggle}`);
  NioApp.Dropdown.header(`.${nav.classes.toggle}`);
  NioApp.Toggle.class(`.toggle-ibx-aside`, 'toggle-active', 'show-aside');
  NioApp.Toggle.class(`.toggle-ibx-view`, 'view-active', 'show-ibx');
  NioApp.Toggle.class(`.toggle-chat-profile`, 'active', 'show-profile','profile-shown');
  NioApp.Custom.setBg();
  NioApp.Custom.todoCheckboxToggle('.todo-check-toggle');
  NioApp.Toggle.class('.toggle-todo-aside', 'toggle-active', 'show-aside');
}

/*  =======================================================
  Bootstrap Scripts init 
========================================================== */
NioApp.BS.init = function () {
  NioApp.BS.tooltip('[data-bs-toggle="tooltip"]');
  NioApp.BS.popover('[data-bs-toggle="popover"]');
  NioApp.BS.toast('[data-bs-toggle="toast"]');
  NioApp.BS.alert('[data-bs-toggle="alert"]');
  NioApp.BS.alert('.custom-alert-trigger',{
    target:"customAlertPlaceholder",
    variant:"warning",
    content:"Some aweosme alert text from JavaScript!",
  });
  NioApp.BS.validate('.form-validate');
}

// Initial by default
/////////////////////////////
NioApp.init = function () {
  NioApp.winLoad(NioApp.BS.init);
  NioApp.winLoad(NioApp.Addons.init);
  NioApp.winLoad(NioApp.Custom.init);
}

NioApp.init();

return NioApp;
})(NioApp);
