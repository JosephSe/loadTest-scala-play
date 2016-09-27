var app = angular.module('StarterApp', ['ngMaterial', 'chart.js', 'datatables']);

app.controller('AppCtrl', ['$rootScope', '$scope', '$http', '$interval', function ($rootScope, $scope, $http, $interval) {
    $scope.isCollapsed = true;
    $scope.dataLoading = true;
    $scope.charts = ['Pie'];
    $scope.chartDataHotel = {
        labels: ["Matching", "Nova > Legacy", "Legacy > Nova"],
        data: {},
        pieData:[]
    };
    $scope.chartDataPrice = {
         labels: ["Matching", "Nova > Legacy", "Legacy > Nova"],
        data: {},
        pieData:[]
    };
    $scope.tickets = [];

    $scope.getTickets = function () {
        $http({
          method: 'GET',
          url: '/jira/filter/19366/tickets'
        }).then(function successCallback(response) {
            var vm = this;
           $scope.tickets = response.data;
          }, function errorCallback(response) {
         });
    }

    $scope.loadChart = function() {
        $scope.dataLoading = true;
        $http({
          method: 'GET',
          url: '/comp/chart/pie'
        }).then(function successCallback(response) {
            $scope.dataLoading = false;
            loadData(response.data);
          }, function errorCallback(response) {
          });
      }

      function loadData(response) {
          angular.forEach(response, function(value) {
            if(value.typ === "Price") {
                $scope.chartDataPrice.data = value;
                $scope.chartDataPrice.pieData = getData(value);
            } else if(value.typ == "Hotels") {
                $scope.chartDataHotel.data = value;
                $scope.chartDataHotel.pieData = getData(value);
            }
          })
      }
      function getData(values) {
        var dataArrays = [];
        dataArrays.push(values.bothSamePct);
        dataArrays.push(values.novaPct);
        dataArrays.push(values.legacyPct);
        return dataArrays;
      }

    }
    ])
    .controller('JiraTicketController', ['$http', 'datatables'], function($http, datatables) {
    })
    .config(function (ChartJsProvider) {
        // Configure all charts
        ChartJsProvider.setOptions({
            colours: ['#97BBCD', '#DCDCDC', '#F7464A', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360'],
            responsive: true
        });
        // Configure all doughnut charts
    })
    .config(function ($mdThemingProvider) {
        $mdThemingProvider.theme('default')
            .primaryPalette('blue-grey')
            .accentPalette('orange');
    });

function comparisonData(){
    var roomRawData = new Array();
        roomRawData[0] = Math.floor((Math.random() * 100) + 1);
        roomRawData[1] = Math.floor((Math.random() * 100) + 1);
        roomRawData[2] = Math.floor((Math.random() * 100) + 1);
		roomRawData[3] = Math.floor((Math.random() * 100) + 1);
    return roomRawData;}