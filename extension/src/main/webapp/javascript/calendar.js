(function($, fullcalendar) {
    var calendar = {
        build: function(selector) {
            $('#'+selector).fullCalendar({
                header: {
                    left: 'prev,next today',
                    center: 'title',
                    right: 'month,agendaWeek,agendaDay,listWeek'
                },
                defaultDate: new Date(),
                editable: true,
                eventClick: function(calEvent, jsEvent, view) {
                    angular.element(rhCtrl).scope().showVacationRequestById(calEvent.id);
                },
                navLinks: true,
                eventLimit: true,
                events: {
                    url: '/rest/rhrequest/getevents',
                    error: function() {
                        $('#script-warning').show();
                    }
                },
                loading: function(bool) {
                    $('#loading').toggle(bool);
                }
            });

        }
    };

    return calendar;
})($, fullcalendar);