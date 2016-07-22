var app = angular.module('StarterApp', ['ngMaterial', 'ngMessages', 'ngMdIcons']);

app.controller('AppCtrl', ['$rootScope', '$scope', '$http', '$log', '$mdMedia', '$mdUtil',
        function($rootScope, $scope, $http, $log, $mdMedia, $mdUtil) {
            var hostName = window.location.host;
            $scope.searchEnabled = false;
            $scope.dataLoading = true;
            $scope.charts = ['Pie'];
            $scope.jobChartData = [];
            $scope.serverResponse = {};

            //new
            google.charts.load('current', {
                'packages': ['annotationchart', 'line']
            });
            //      google.charts.load('current', {'packages':['annotationchart', 'corechart', 'line']});
            google.charts.setOnLoadCallback(drawLogScales);

            $scope.initChart = function() {
                $scope.dataLoading = true;
                $http({
                    method: 'GET',
                    url: '/jenkins/job/history/all'
                }).then(function successCallback(response) {
                    $scope.dataLoading = false;
                    $scope.serverResponse = response.data;
                    google.charts.setOnLoadCallback(drawLogScales);
                    //                drawLogScales(response.data);
                }, function errorCallback(response) {});
            }

            function loadData(response) {
                var labels = ["passed", "skipped", "failed"];
                angular.forEach(response, function(value) {
                    value.labels = labels;
                    //              value.running = true;
                    value.pieData = [];
                    value.pieData.push(value.totalCount - value.failCount - value.skipCount);
                    value.pieData.push(value.skipCount);
                    value.pieData.push(value.failCount);
                    $scope.jobChartData.push(value);
                })
            }

            function drawLogScales() {
                var response = $scope.serverResponse;
                //                      var tmp = new google.visualization.DataTable();
                var data = new google.visualization.DataTable();
                var tempArray = [];
                data.addColumn('number', 'Build number');
                var keysArray = [];
                angular.forEach(response, function(value, key) {
                    data.addColumn('number', key);
                    keysArray.push(key);
                });
                var size = response.size;
                data.addRows(100);

                for (i = 0; i < 100; i++) {
                    data.setCell(i, 0, i)
                    for (j = 0; j < keysArray.length; j++) {
                        data.setCell(i, j + 1, (j / 2) + getStatusCode(response[keysArray[j]][i]))
                    }
                }

                var options = {
                    chart: {
                        title: 'Production system status'
                    },
                    hAxis: {
                        title: 'System Status',
                        baselineColor: '#fff',
                        gridlineColor: '#fff',
                        logScale: false
                    },
                    vAxis: {
                        baselineColor: '#fff',
                        gridlineColor: '#fff',
                        textPosition: 'none'
                    },
                    colors: ['#097138', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360', '#97BBCD'],
//                    smoothLine: true,
                    animation:{
                        duration: 1000,
                        easing: 'inAndOut',
//                        easing: 'out',
                        startup: true
                     },
                    chartArea: {
                        left: 20,
                        top: 15,
                        width: '65%',
                        height: '75%'
                    }
                    //                        legend: { position: 'bottom' }
                };

                var chart = new google.visualization.LineChart(document.getElementById('coreChart_div'));
                chart.draw(data, options);
            }

            function getStatusCode(job) {
                var status = job.result
                if (status === "SUCCESS" || status === "BUILDING") return 1
                else if (status === "ABORTED") return -1
                else if (status === "FAILURE") return 0
            }

        }
    ])
    .config(function($mdThemingProvider) {
        $mdThemingProvider.theme('default')
            .primaryPalette('blue-grey')
            .accentPalette('orange')
            .warnPalette('red')
            .backgroundPalette('grey');
    });