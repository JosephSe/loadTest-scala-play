var app = angular.module('StarterApp', ['chart.js', 'datatables']);

app.controller('AppCtrl', ['$rootScope', '$scope', '$http', function ($rootScope, $scope, $http) {
    $scope.isCollapsed = true;
    $scope.dataLoading = true;
    $scope.charts = ['Pie'];
    $scope.jobs = [{"name":"HTML_Pricing_SIT","buildNo":129,"failCount":214,"skipCount":0,"totalCount":798},{"name":"HTML_Itinerary_SIT","buildNo":19,"failCount":37,"skipCount":0,"totalCount":46},{"name":"Expedia_Integration_SIT","buildNo":22,"failCount":0,"skipCount":0,"totalCount":0}];
    $scope.jobChartData = [{
        name : "test1",
        labels: ["failed", "skipped", "total"],
        data: {},
        pieData:[214,0,789]
    },
    {
        name : "test2",
        labels: ["failed", "skipped", "total"],
        data: {},
        pieData:[214,0,789]
    }];
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
            colours: ['#DCDCDC', '#F7464A', '#97BBCD', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360'],
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