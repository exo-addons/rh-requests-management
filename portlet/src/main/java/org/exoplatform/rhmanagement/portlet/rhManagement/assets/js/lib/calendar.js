
define('calendar', ["SHARED/jquery","fullcalendar"], function($,fullcalendar) {
    var require = eXo.require, requirejs = eXo.require,define = eXo.define;
    eXo.define.names=["$","fullcalendar"];
    eXo.define.deps=[$,fullcalendar];
    return (function($, fullcalendar) {
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
                        angular.element(rhCtrl).scope().showVacationResume(calEvent.id);
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
});