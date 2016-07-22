var app = angular.module('StarterApp', ['ngMaterial', 'ngMessages', 'ngMdIcons', 'ngCookies', 'chart.js']);

app.controller('AppCtrl', ['$rootScope', '$scope', '$mdSidenav', '$http', '$log', '$mdBottomSheet', '$mdDialog', '$mdMedia', '$mdToast', '$mdUtil',
        function($rootScope, $scope, $mdSidenav, $http, $log, $mdBottomSheet, $mdDialog, $mdMedia, $mdToast, $mdUtil) {
            var hostName = window.location.host;
            $scope.searchEnabled = false;

    $scope.isCollapsed = true;
    $scope.dataLoading = true;
    $scope.charts = ['Pie'];
    $scope.jobs = [{"name":"HTML_Pricing_SIT","buildNo":129,"failCount":214,"skipCount":0,"totalCount":798},{"name":"HTML_Itinerary_SIT","buildNo":19,"failCount":37,"skipCount":0,"totalCount":46},{"name":"Expedia_Integration_SIT","buildNo":22,"failCount":0,"skipCount":0,"totalCount":0}];
    $scope.jobChartData = [];

    $scope.initChart = function() {
        $scope.dataLoading = true;
        $http({
          method: 'GET',
          url: '/jenkins/job/all'
        }).then(function successCallback(response) {
            $scope.dataLoading = false;
            loadData(response.data);
            $('#dataTables-prices').DataTable({
            responsive: true
            });
          }, function errorCallback(response) {
          });
      }

      function loadData(response) {
        var labels = ["passed", "skipped", "failed"];
          angular.forEach(response, function(value) {
              value.labels = labels;
//              value.running = true;
              value.pieData=[];
              value.pieData.push(value.totalCount-value.failCount-value.skipCount);
              value.pieData.push(value.skipCount);
              value.pieData.push(value.failCount);
              $scope.jobChartData.push(value);
          })
      }
            $scope.toggleRight = buildToggler('right');
            $scope.toggleLeft = buildToggler('left');

            $scope.toggleSidenav = function(menuId) {
                $mdSidenav(menuId).toggle();
            };
            /**
             * Build handler to open/close a SideNav; when animation finishes
             * report completion in console
             */
            function buildToggler(navID) {
                var debounceFn = $mdUtil.debounce(function() {
                    $mdSidenav(navID)
                        .toggle()
                        .then(function() {
                            $log.debug("toggle " + navID + " is done");
                        });
                }, 300);

                return debounceFn;
            };

            $scope.showWhatsNew = function($event) {
                  $mdDialog.show({
                    controller: 'DialogController',
                    parent: angular.element(document.body),
                    targetEvent: $event,
                    clickOutsideToClose:true,
                    templateUrl: 'whatsnew.tmpl.html'
                  })
                  .then(function(answer) {
                      }, function() {
                      });
              };

          $scope.showGridBottomSheet = function() {
              $scope.alert = '';
              $mdBottomSheet.show({
                templateUrl: 'bottom-sheet-grid-template.html',
                controller: 'GridBottomSheetCtrl',
                clickOutsideToClose: false
              }).then(function(clickedItem) {
                $mdToast.show(
                      $mdToast.simple()
                        .textContent(clickedItem['name'] + ' clicked!')
                        .position('top right')
                        .hideDelay(1500)
                    );
              });
            };
        }
    ])
    .controller('GridBottomSheetCtrl', function($scope, $mdBottomSheet) {
      $scope.items = [
        { name: 'Scala', icon: 'images/icons/Scala_logo.png', width:"225px", height:"50px", url:"http://www.scala-lang.org"},
        { name: 'AngularJS', icon: 'images/icons/angularLogo.png', width:"225px", height:"50px", url:"https://angularjs.org"},
        { name: 'Play Framework', icon: 'images/icons/play_full_color.png', width:"150px", height:"50px", url:"https://www.playframework.com"},
        { name: 'MongoDB', icon: 'images/icons/mongodb.svg', width:"300px", height:"50px", url:"https://www.mongodb.org"},
        { name: 'Akka', icon: 'images/icons/akka.png', width:"140px", height:"40px", url:"http://akka.io/"},
        { name: 'Reactive Mongo', icon: 'images/icons/reactive-mongo-logo.png', width:"200px", height:"45px", url:"http://reactivemongo.org/"},
      ];

      $scope.listItemClick = function($index) {
        var clickedItem = $scope.items[$index];
        $mdBottomSheet.hide(clickedItem);
      };
    })
    .run(function($http, $templateCache) {

        var urls = [
          'images/icons/Scala_logo.png'
        ];

        angular.forEach(urls, function(url) {
          $http.get(url, {cache: $templateCache});
        });

      })
    .config(function($mdThemingProvider) {
        $mdThemingProvider.theme('default')
            .primaryPalette('blue-grey')
            .accentPalette('orange')
            .warnPalette('red')
            .backgroundPalette('grey');
    })
    .config(function (ChartJsProvider) {
        // Configure all charts
        ChartJsProvider.setOptions({
            colours: ['#3fa7de', '#DCDCDC', '#F7464A', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360', '#97BBCD'],
            responsive: true
        });
        // Configure all doughnut charts
    })
    ;