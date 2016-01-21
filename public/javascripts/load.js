var app = angular.module('StarterApp', ['chart.js', 'datatables']);

app.controller('AppCtrl', ['$rootScope', '$scope', '$http', '$interval', function ($rootScope, $scope, $http, $interval) {
    $scope.isCollapsed = true;
    $scope.dataLoading = true;
    $scope.charts = ['Pie'];
    $scope.chartDataHotel = {
        labels: ["Hotels Matching", "Nova > Legacy", "Legacy > Nova", "Zero Hotels returned"],
        data: {},
        pieData:[]
    };
    $scope.chartDataPrice = {
         labels: ["Prices Matching", "Nova > Legacy", "Legacy > Nova", "Prices NA"],
        data: {},
        pieData:[]
    };
    $scope.chartDataRoom = {
        labels: ["Rooms Matching", "Nova > Legacy", "Legacy > Nova", "Zero Rooms returned"],
        data: {},
        pieData:[]
    };

    $scope.initChart = function() {
        $scope.dataLoading = true;
        $http({
          method: 'GET',
          url: '/chart/pie'
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
          angular.forEach(response, function(value) {
            if(value.typ === "Price") {
                $scope.chartDataPrice.data = value;
                $scope.chartDataPrice.pieData = getData(value);
            } else if(value.typ == "Hotels") {
                $scope.chartDataHotel.data = value;
                $scope.chartDataHotel.pieData = getData(value);
            } else if (value.typ =="Rooms") {
                $scope.chartDataRoom.data = value;
                $scope.chartDataRoom.pieData = getData(value);
            }
          })
      }
      function getData(values) {
        var dataArrays = [];
        dataArrays.push(values.bothSame);
        dataArrays.push(values.nova);
        dataArrays.push(values.legacy);
        dataArrays.push(values.bothZeroHotels);
        return dataArrays;
      }

    }
    ])
    .config(function (ChartJsProvider) {
        // Configure all charts
        ChartJsProvider.setOptions({
            colours: ['#97BBCD', '#DCDCDC', '#F7464A', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360'],
            responsive: true
        });
        // Configure all doughnut charts
    });

function comparisonData(){
    var roomRawData = new Array();
        roomRawData[0] = Math.floor((Math.random() * 100) + 1);
        roomRawData[1] = Math.floor((Math.random() * 100) + 1);
        roomRawData[2] = Math.floor((Math.random() * 100) + 1);
		roomRawData[3] = Math.floor((Math.random() * 100) + 1);
    return roomRawData;}