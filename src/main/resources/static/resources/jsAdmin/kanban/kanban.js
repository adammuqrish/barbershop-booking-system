!(function (NioApp) {
"use strict";

NioApp.kTemplates = {
    title : function (title, count){
        return (`
            <div class="kanban-title-content">
                <h6 class="title">${title}</h6>
                <span class="count">${count}</span>
            </div>
        `)
    },
    content : function (title, description){
        return (`
            <div class="kanban-item-title">
                <h6 class="title">${title}</h6>
            </div>
            <div class="kanban-item-text">
                <p>${description}</p>
            </div>
        `)
    }
}

let  kanbanDefault ={
    options : {
        gutter:'0',
    },
    boards: [{
        'id': '_inprocess',
        'title': 'In Process',
        'item': [{
                'title': '<span>You can drag me too</span>'
            },
            {
                'title': '<span>Buy Milk</span>'
            }
        ]
    }, {
        'id': '_working',
        'title': 'Working',
        'item': [{
                'title': '<span>Do Something!</span>'
            },
            {
                'title': '<span>Run?</span>'
            }
        ]
    }, {
        'id': '_done',
        'title': 'Done',
        'item': [{
                'title': '<span>All right</span>'
            },
            {
                'title': '<span>Ok!</span>'
            }
        ]
    }]
} 

let kanbanColored = {
    options : {
        gutter:'0'
    },
    boards:[{
        'id': '_inprocess',
        'title': 'In Process',
        'item': [{
                'title': '<span>You can drag me too</span>',
            },
            {
                'title': '<span>Buy Milk</span>',
            }
        ]
    }, {
        'id': '_working',
        'title': 'Working',
        'item': [{
                'title': '<span>Do Something!</span>',
            },
            {
                'title': '<span>Run?</span>',
            }
        ]
    }, {
        'id': '_done',
        'title': 'Done',
        'item': [{
                'title': '<span>All right</span>',
            },
            {
                'title': '<span>Ok!</span>',
            }
        ]
    }]
}

let kanbanCustomBoard ={
    options : {
        gutter:'0'
    },
    boards:[
        {
            'id': '_open',
            'title': NioApp.kTemplates.title('Open', '3'),
            'class': 'kanban-light',
            'item': [{
                'title': NioApp.kTemplates.content('NioBoard Design Kit Update', 'Update the new UI design for nioboard template with based on feedback'),
            },{
                'title': NioApp.kTemplates.content('Implement Design into Template','Start implementing new design in Coding nioboard.'),
            },{
                'title': NioApp.kTemplates.content('NioBoard React Version','Implement new UI design in react version nioboard template as soon as possible.'),
            }]
        },
        {
            'id': '_in_progress',
            'title': NioApp.kTemplates.title('In Progress', '4'),
            'class': 'kanban-primary',
            'item': [{
                'title':NioApp.kTemplates.content('Techyspec Keyword Research','Keyword recarch for techyspec business profile and there other websites, to improve ranking.'),
            },{
                'title':NioApp.kTemplates.content('Fitness Next Website Design','Design a awesome website for fitness_next new product launch.'),
            },{
                'title':NioApp.kTemplates.content('Runnergy Website Redesign','Redesign there old/backdated website new modern and clean look keeping minilisim in mind.'),
            },{
                'title':NioApp.kTemplates.content('Wordlab Android App','Wordlab Android App with with react native.'),
            }]
        },
        {
            'id': '_to_review',
            'title': NioApp.kTemplates.title('In Review', '2'),
            'class': 'kanban-warning',
            'item': [{
                'title':NioApp.kTemplates.content('Oberlo Development','Complete website development for Oberlo limited.'),
            },{
                'title':NioApp.kTemplates.content('IOS app for Getsocio','Design and develop app for Getsocio IOS.'),
            },{
                'title':NioApp.kTemplates.content('IOS app for Getsocio','Design and develop app for Getsocio IOS.'),
            }]
        },
        {
            'id': '_issues',
            'title': NioApp.kTemplates.title('Issues', '1'),
            'class': 'kanban-danger',
            'item': [{
              'title':NioApp.kTemplates.content('Qualitative research planning','Complete website development for Oberlo limited.'),
          }]
        },
        {
            'id': '_completed',
            'title': NioApp.kTemplates.title('Completed', '0'),
            'class': 'kanban-success',
            'item': []
        },
    ]
}

NioApp.Kanban = function(selector,boards) {

    let elm = document.querySelectorAll(selector);
    if( elm.length > 0 ){
        elm.forEach(item => {
            let getData = item.id && eval(item.id);
            let getOptions = getData.options ? getData.options : false;
            let setBoard = (typeof boards === 'undefined') ? getData.boards : boards;
            let kanban = new jKanban({
                element: `#${item.id}`,
                gutter: getOptions.gutter ? getOptions.gutter : 0,
                widthBoard: getOptions.widthBoard ? getOptions.widthBoard : '250px',
                boards: setBoard
            });
        })
    }
}
NioApp.Kanban.init = function () {
    NioApp.Kanban('.js-kanban');
}
NioApp.winLoad(NioApp.Kanban.init);

})(NioApp);