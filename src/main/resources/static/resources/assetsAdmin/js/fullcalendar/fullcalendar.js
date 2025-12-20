!(function (NioApp) {
    "use strict";
    
    NioApp.Event = {
        preview: function (){
            let eventPreviewModal = `
                <div class="modal fade" tabindex="-1" id="eventPreviewModal">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title event-title"></h4>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <div>
                                <div class="row g-3">
                                    <div class="col-6">
                                        <h6 class="overline-title">Start Time</h6>
                                        <p class="event-start small"></p>
                                    </div>
                                    <div class="col-6">
                                        <h6 class="overline-title">End Time</h6>
                                        <p class="event-end small"></p>
                                    </div>
                                    <div class="col-12">
                                        <h6 class="overline-title">Event Details</h6>
                                        <p class="event-description small"></p>
                                    </div>
                                </div>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#eventDeleteModal">Delete</button>
                                <button type="button" class="btn btn-primary" id="editEvent">Edit Event</button>
                            </div>
                        </div>
                    </div>
                </div>
            `
            NioApp.body.insertAdjacentHTML('beforeend', eventPreviewModal);
        },
        delete: function (){
            let eventDeleteModal = `
                <div class="modal fade" tabindex="-1" id="eventDeleteModal">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-body p-5 text-center">
                                <div class="media media-circle media-xxl media-middle mx-auto text-bg-danger mb-4"><em class="icon ni ni-cross"></em></div>
                                <h3>Are You Sure?</h3>
                                <p class="small">This event data will be removed permanently.</p>
                                <ul class="d-flex gap g-3 justify-content-center pt-1">
                                    <li><button type="button" class="btn btn-success" id="deleteEvent" data-bs-dismiss="modal">Yes Delete it!</button></li>
                                    <li><button type="button" class="btn btn-danger btn-soft" data-bs-dismiss="modal">Cancel</button></li>
                                </ul>
                            </div>
                            <button type="button" class="btn-close position-absolute top-0 end-0 p-3" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                    </div>
                </div>
            `
            NioApp.body.insertAdjacentHTML('beforeend', eventDeleteModal);
        },
        form: function (){
            let eventFormModal = `
                <div class="modal fade" tabindex="-1" id="eventFormModal">
                    <div class="modal-dialog modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title fc-form-title"></h4>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <div class="row g-gs">
                                    <div class="col-12">
                                        <div class="form-group">
                                            <label for="eventTitle" class="form-label">Event Title</label>
                                            <div class="form-control-wrap">
                                                <input type="text" class="form-control" id="eventTitle" placeholder="Event Title">
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-sm-6">
                                        <div class="form-group">
                                            <label for="eventStartDate" class="form-label">Event Starts</label>
                                            <div class="row g-2">
                                                <div class="col-7">
                                                    <div class="form-control-wrap">
                                                        <input type="text" class="form-control fc-event-datepicker" id="eventStartDate" placeholder="yyyy-mm-dd">
                                                    </div>
                                                </div>
                                                <div class="col-5">
                                                    <div class="form-control-wrap">
                                                        <input type="text" class="form-control fc-event-timepicker" id="eventStartTime" placeholder="hh-mm">
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-sm-6">
                                        <div class="form-group">
                                            <label for="eventEndDate" class="form-label">Event Ends</label>
                                            <div class="row g-2">
                                                <div class="col-7">
                                                    <div class="form-control-wrap">
                                                        <input type="text" class="form-control fc-event-datepicker" id="eventEndDate" placeholder="yyyy-mm-dd">
                                                    </div>
                                                </div>
                                                <div class="col-5">
                                                    <div class="form-control-wrap">
                                                        <input type="text" class="form-control fc-event-timepicker" id="eventEndTime" placeholder="hh-mm">
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-12">
                                        <div class="form-group">
                                            <label for="eventDescription" class="form-label">Event Description</label>
                                            <div class="form-control-wrap">
                                                <textarea class="form-control" id="eventDescription" placeholder="Event Description"></textarea>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-12">
                                        <div class="form-group fc-category-select">
                                            <label for="eventCategory" class="form-label">Event Category</label>
                                            <div class="form-control-wrap">
                                                <select id="eventCategory" class="form-control"></select>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-danger" data-bs-dismiss="modal">Discard</button>
                                <button type="button" class="btn btn-primary" id="eventAdd">Add Event</button>
                                <button type="button" class="btn btn-primary" id="eventUpdate">Update Event</button>
                            </div>
                        </div>
                    </div>
                </div>
            `
            NioApp.body.insertAdjacentHTML('beforeend', eventFormModal);
        },
    }

    NioApp.Calendar = function (selector,events) {
        let elm = document.querySelectorAll(selector);

        elm.forEach(item => {
            let calId = item.id;
            let calendarEl = document.getElementById(calId);

            let removePopover = function() {
                let fcPopover = document.querySelectorAll('.event-popover');
                fcPopover.forEach(elm => {
                    elm.remove();
                })
            }
              
            if(calendarEl != 'undefined' && calendarEl != null){
                let calendar = new FullCalendar.Calendar(calendarEl, {
                    timeZone: 'UTC',
                    initialView: NioApp.State.asMobile ? 'listWeek' : 'dayGridMonth',
                    themeSystem: 'bootstrap',
                    headerToolbar: {
                        left: 'title prev,next',
                        center: null,
                        right: 'today dayGridMonth,timeGridWeek,timeGridDay,listWeek'
                    },
                    height: 800,
                    contentHeight: 780,
                    aspectRatio: 3,
                    editable: true,
                    droppable: true,
                    views: {
                        dayGridMonth: {
                            dayMaxEventRows: 2,
                        }
                    },
                    direction: NioApp.State.isRTL ? "rtl" : "ltr",
                    nowIndicator: true,
                    eventMouseEnter: function(item) {
                        let elm = item.el, title = item.event._def.title, content = item.event._def.extendedProps.description;
                        if(content){
                            const popover = new bootstrap.Popover(item.el, {
                                template: '<div class="popover event-popover"><div class="popover-arrow"></div><h3 class="popover-header"></h3><div class="popover-body"></div></div>',
                                title: title,
                                content: content ? content : '',
                                placement: 'top',
                            })
                            popover.show();
                        }

                    },
                    eventMouseLeave: function(){
                        removePopover();
                    },
                    eventDragStart: function(){
                        removePopover();
                    },
                    eventClick: function(item){
                        let title = item.event._def.title;
                        let description = item.event._def.extendedProps.description;
                        let start = item.event._instance.range.start;
                        let startDate = start.getFullYear() + '-' + String(start.getMonth() + 1).padStart(2, '0') + '-' + String(start.getDate()).padStart(2, '0');
                        let startDateAlt = String(start.getDate()).padStart(2, '0') + ' ' + NioApp.monthList[start.getMonth()] + ' ' + start.getFullYear()
                        let startTime = start.toUTCString().split(' '); startTime = startTime[startTime.length-2]; startTime = (startTime == '00:00:00') ? '' : startTime;
                        let end = item.event._instance.range.end;
                        let endDate = end.getFullYear() + '-' + String(end.getMonth() + 1).padStart(2, '0') + '-' + String(end.getDate()).padStart(2, '0');
                        let endDateAlt = String(end.getDate()).padStart(2, '0') + ' ' + NioApp.monthList[end.getMonth()] + ' ' + end.getFullYear()
                        let endTime = end.toUTCString().split(' '); endTime = endTime[endTime.length-2]; endTime = (endTime == '00:00:00') ? '' : endTime;
                        let className = item.event._def.ui.classNames[0].slice(3);
                        let eventId = item.event._def.publicId;
                        
                        //set seleted event
                        selectedEvent = calendar.getEventById(eventId);
                        selectedEventId = eventId;
                    
                        // preview
                        previewTitle.innerText = title;
                        previewDescription.innerText = description;
                        previewStart.innerText = `${startDateAlt}${startTime ? ' - ' + NioApp.to12(startTime) : ''}`;
                        previewEnd.innerText = `${endDateAlt}${endTime ? ' - ' + NioApp.to12(endTime) : ''}`;
                        
                        previewModal.show();

                    },
                    events: events
                });
                
                calendar.render();

                // init modal markups
                NioApp.Event.preview();
                NioApp.Event.delete();
                NioApp.Event.form();
                // init date picker
                NioApp.DatePicker('.fc-event-datepicker',{
                    buttonClass: 'btn',
                    autohide:true,
                    format: 'yyyy-mm-dd'
                });
                // init time picker
                NioApp.TimePicker('.fc-event-timepicker',{
                    format: 24,
                    interval : 30,
                    start : '00:00',
                    end : '23:30',
                });

                // preview modal delector
                let eventPreviewModal = document.getElementById('eventPreviewModal');
                let previewTitle = eventPreviewModal.querySelector('.event-title');
                let previewDescription = eventPreviewModal.querySelector('.event-description');
                let previewStart = eventPreviewModal.querySelector('.event-start');
                let previewEnd = eventPreviewModal.querySelector('.event-end');
                
                // create preview modal
                const previewModal = new bootstrap.Modal(eventPreviewModal, {
                    keyboard: false
                })

                // choice js for eventcategory
                let eventCategorySelect = document.getElementById('eventCategory');
                const choicesCat = new Choices(eventCategorySelect, {
                    silent: true,
                    allowHTML: false,
                    searchEnabled: true,
                    placeholder: true,
                    placeholderValue: null,
                    searchPlaceholderValue: 'Search',
                    shouldSort: false,
                    choices: [
                        { value: 'event-primary', label: 'Company' },
                        { value: 'event-success', label: 'Seminars' },
                        { value: 'event-info', label: 'Conferences' },
                        { value: 'event-warning', label: 'Meeting' },
                        { value: 'event-danger', label: 'Business dinners' },
                        { value: 'event-dark', label: 'Private' },
                        { value: 'event-primary-soft', label: 'Auctions' },
                        { value: 'event-success-soft', label: 'Networking events' },
                        { value: 'event-info-soft', label: 'Product launches' },
                        { value: 'event-warning-soft', label: 'Fundrising' },
                        { value: 'event-danger-soft', label: 'Sponsored' },
                        { value: 'event-dark-soft', label: 'Sports events' },
                    ],
                    callbackOnCreateTemplates: function(template) {
                        return{
                            item: ({ classNames }, data) => {
                                return template(`
                                <div class="${classNames.item} ${
                                data.highlighted
                                    ? classNames.highlightedState
                                    : classNames.itemSelectable
                                } ${
                                data.placeholder ? classNames.placeholder : ''
                                }" data-item data-id="${data.id}" data-value="${data.value}" ${
                                data.active ? 'aria-selected="true"' : ''
                                } ${data.disabled ? 'aria-disabled="true"' : ''}>
                                    <span class="fc-select-dot fc-${data.value}"></span> ${data.label}
                                </div>
                                `);
                            },
                            choice: ({ classNames }, data) => {
                                return template(`
                                <div class="${classNames.item} ${classNames.itemChoice} ${
                                data.disabled ? classNames.itemDisabled : classNames.itemSelectable
                                }" data-select-text="${this.config.itemSelectText}" data-choice ${
                                data.disabled
                                    ? 'data-choice-disabled aria-disabled="true"'
                                    : 'data-choice-selectable'
                                } data-id="${data.id}" data-value="${data.value}" ${
                                data.groupId > 0 ? 'role="treeitem"' : 'role="option"'
                                }>
                                    <span class="fc-select-dot fc-${data.value}"></span> ${data.label}
                                </div>
                                `);
                            },
                        }
                    }
                });

                let selectedEvent,selectedEventId;
                
                //form modal selectors
                let deleteEvent = document.getElementById('deleteEvent');
                let editEvent = document.getElementById('editEvent');
                let addEvent = document.getElementById('addEvent');
                let eventFormModal = document.getElementById('eventFormModal');
                let formTitle = eventFormModal.querySelector('.fc-form-title');
                let formAction = eventFormModal.querySelector('.fc-form-action');
                let formInputTitle = eventFormModal.querySelector('#eventTitle');
                let formInputDescription = eventFormModal.querySelector('#eventDescription');
                let formInputCategory = eventFormModal.querySelector('#eventCategory');
                let formInputStartDate = eventFormModal.querySelector('#eventStartDate');
                let formInputStartTime = eventFormModal.querySelector('#eventStartTime');
                let formInputEndDate = eventFormModal.querySelector('#eventEndDate');
                let formInputEndTime = eventFormModal.querySelector('#eventEndTime');
                let formEventAdd = eventFormModal.querySelector('#eventAdd');
                let formEventUpdate = eventFormModal.querySelector('#eventUpdate');

                const formModal = new bootstrap.Modal(eventFormModal, {
                    keyboard: false
                })

                //delete event
                deleteEvent.addEventListener('click', function(e){
                    e.preventDefault();
                    selectedEvent.remove();
                })
                
                //get data from seleted event
                editEvent.addEventListener('click', function(e){
                    e.preventDefault();
                    previewModal.hide();
                    formModal.show();

                    let start = selectedEvent._instance.range.start;
                    let startDate = start.getFullYear() + '-' + String(start.getMonth() + 1).padStart(2, '0') + '-' + String(start.getDate()).padStart(2, '0');
                    let startTime = start.toUTCString().split(' '); startTime = startTime[startTime.length-2]; startTime = (startTime == '00:00:00') ? '' : startTime;
                    let end = selectedEvent._instance.range.end;
                    let endDate = end.getFullYear() + '-' + String(end.getMonth() + 1).padStart(2, '0') + '-' + String(end.getDate()).padStart(2, '0');
                    let endTime = end.toUTCString().split(' '); endTime = endTime[endTime.length-2]; endTime = (endTime == '00:00:00') ? '' : endTime;

                    formTitle.innerText = 'Edit Event';
                    formInputTitle.value = selectedEvent._def.title;
                    formInputDescription.value = selectedEvent._def.extendedProps.description ? selectedEvent._def.extendedProps.description : '';
                    formInputStartDate.value = startDate;
                    formInputStartTime.value = startTime;
                    formInputEndDate.value = endDate;
                    formInputEndTime.value = endTime;
                    choicesCat.setChoiceByValue(selectedEvent._def.ui.classNames[0].slice(3));
                    formEventAdd.style.display = 'none';
                    formEventUpdate.style.display = 'block';
                })
                
                //reset data for new event
                addEvent.addEventListener('click', function(e){
                    e.preventDefault();
                    formTitle.innerText = 'Add Event';
                    formInputTitle.value = '';
                    formInputDescription.value = '';
                    formInputStartDate.value = '';
                    formInputStartTime.value = '';
                    formInputEndDate.value = '';
                    formInputEndTime.value = '';
                    choicesCat.setChoiceByValue('event-primary');
                    formEventAdd.style.display = 'block';
                    formEventUpdate.style.display = 'none';
                })

                //on update button click
                formEventUpdate.addEventListener('click', function(e){
                    e.preventDefault();
                    selectedEvent.remove();
                    calendar.addEvent({
                        id: selectedEventId,
                        title: formInputTitle.value,
                        start: formInputStartDate.value + (formInputStartTime.value ? 'T' + formInputStartTime.value + 'Z' : ''),
                        end: formInputEndDate.value + (formInputEndTime.value ? 'T' + formInputEndTime.value + 'Z' : ''),
                        className: `fc-${formInputCategory.value}`,
                        description: formInputDescription.value,
                    });
                    formModal.hide();
                })

                //on add button click
                formEventAdd.addEventListener('click', function(e){
                    e.preventDefault();
                    calendar.addEvent({
                        id: `added-event-id-${Math.floor(Math.random()*9999999)}`,
                        title: formInputTitle.value,
                        start: formInputStartDate.value + (formInputStartTime.value ? 'T' + formInputStartTime.value + 'Z' : ''),
                        end: formInputEndDate.value + (formInputEndTime.value ? 'T' + formInputEndTime.value + 'Z' : ''),
                        className: `fc-${formInputCategory.value}`,
                        description: formInputDescription.value,
                    });
                    formModal.hide();
                })
            }

        })
    }

    NioApp.Time = {
        today : function(){
            let today = new Date();
            let dd = String(today.getDate()).padStart(2, '0');
            let mm = String(today.getMonth() + 1).padStart(2, '0');
            let yyyy = today.getFullYear();

            return yyyy + '-' + mm + '-' + dd;
        },
        yesterday : function(){
            let today = new Date();
            let yesterday = new Date(today);
            yesterday.setDate(today.getDate() - 1);
            let y_dd = String(yesterday.getDate()).padStart(2, '0');
            let y_mm = String(yesterday.getMonth() + 1).padStart(2, '0');
            let y_yyyy = yesterday.getFullYear();

            return  y_yyyy + '-' + y_mm + '-' + y_dd;
        },
        currentMonth : function(){
            let today = new Date();
            let mm = String(today.getMonth() + 1).padStart(2, '0');
            let yyyy = today.getFullYear();

            return yyyy + '-' + mm;
        },
    }

    NioApp.Calendar.init = function () {

        NioApp.Calendar('.js-calendar',[
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'Reader will be distracted',
                start: NioApp.Time.currentMonth() + '-03T13:30:00',
                className: "fc-event-danger",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden. 1",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'Rabfov va hezow.',
                start: NioApp.Time.currentMonth() + '-14T13:30:00',
                end: NioApp.Time.currentMonth() + '-14',
                className: "fc-event-success",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'The leap into electronic',
                start: NioApp.Time.currentMonth() + '-05',
                end: NioApp.Time.currentMonth() + '-06',
                className: "fc-event-primary",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'Lorem Ipsum passage - Product Release',
                start: NioApp.Time.currentMonth() + '-02',
                end: NioApp.Time.currentMonth() + '-04',
                className: "fc-event-primary",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                title: 'Gibmuza viib hepobe.',
                start: NioApp.Time.currentMonth() + '-12',
                end: NioApp.Time.currentMonth() + '-10',
                className: "fc-event-dark-soft",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'Jidehse gegoj fupelone.',
                start: NioApp.Time.currentMonth() + '-07T16:00:00',
                className: "fc-event-danger-soft",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'Ke uzipiz zip.',
                start: NioApp.Time.currentMonth() + '-16T16:00:00',
                end: NioApp.Time.currentMonth() + '-14',
                className: "fc-event-info-soft",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'Piece of classical Latin literature',
                start: NioApp.Time.today(),
                end: NioApp.Time.today() + '-01',
                className: "fc-event-primary",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'Nogok kewwib ezidbi.',
                start: NioApp.Time.today() + 'T10:00:00',
                className: "fc-event-info",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'Mifebi ik cumean.',
                start: NioApp.Time.today() + 'T14:30:00',
                className: "fc-event-warning-soft",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'Play Time',
                start: NioApp.Time.today() + 'T17:30:00',
                className: "fc-event-info",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'Rujfogve kabwih haznojuf.',
                start: NioApp.Time.yesterday() + 'T05:00:00',
                className: "fc-event-danger",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            },
            {
                id: 'default-event-id-' + Math.floor(Math.random()*9999999),
                title: 'simply dummy text of the printing',
                start: NioApp.Time.yesterday() + 'T07:00:00',
                className: "fc-event-primary-soft",
                description: "Use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden.",
            }
        ]);
        
    }

    NioApp.winLoad(NioApp.Calendar.init);

})(NioApp);