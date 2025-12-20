!(function (NioApp) {
"use strict";

NioApp.DataTable = function (selector, opt) {
  let elm = document.querySelectorAll(selector);
    if( elm != 'undefined' && elm != null ){
      elm.forEach(item => {
        let showPerPage = item.dataset.showPerPage ? parseInt(item.dataset.showPerPage) : 10;
        let dataTables = new simpleDatatables.DataTable(item, {
          labels: {
            perPage: "{select} Per page",
          },
          perPage: showPerPage,
          prevText: `<em class="icon ni ni-chevron-left"></em>`,
          nextText: `<em class="icon ni ni-chevron-right"></em>`,
          lastText: `<em class="icon ni ni-chevrons-right"></em>`,
        });
        
        //scrolled rsponsive
        let dataContainer = item.dataset.nkContainer ? item.dataset.nkContainer : '',
        containerClass = dataContainer.split(' ');
        dataContainer && dataTables.container.classList.add(...containerClass)
      })
    }
}

NioApp.DataTable.init = function () {
  NioApp.DataTable('.datatable-init')
}

NioApp.winLoad(NioApp.DataTable.init);

})(NioApp);
