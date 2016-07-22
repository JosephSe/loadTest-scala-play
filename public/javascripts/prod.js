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

/*            function drawLogScales() {
                var data = new google.visualization.DataTable();
                data.addColumn('number', 'X');
                data.addColumn('number', 'Dogs');
                data.addColumn('number', 'Cats');

                data.addRows([
                    [0, 0, 0],
                    [1, 10, 5],
                    [2, 23, 15],
                    [3, 17, 9],
                    [4, 18, 10],
                    [5, 9, 5],
                    [6, 11, 3],
                    [7, 27, 19],
                    [8, 33, 25],
                    [9, 40, 32],
                    [10, 32, 24],
                    [11, 35, 27],
                    [12, 30, 22],
                    [13, 40, 32],
                    [14, 42, 34],
                    [15, 47, 39],
                    [16, 44, 36],
                    [17, 48, 40],
                    [18, 52, 44],
                    [19, 54, 46],
                    [20, 42, 34],
                    [21, 55, 47],
                    [22, 56, 48],
                    [23, 57, 49],
                    [24, 60, 52],
                    [25, 50, 42],
                    [26, 52, 44],
                    [27, 51, 43],
                    [28, 49, 41],
                    [29, 53, 45],
                    [30, 55, 47],
                    [31, 60, 52],
                    [32, 61, 53],
                    [33, 59, 51],
                    [34, 62, 54],
                    [35, 65, 57],
                    [36, 62, 54],
                    [37, 58, 50],
                    [38, 55, 47],
                    [39, 61, 53],
                    [40, 64, 56],
                    [41, 65, 57],
                    [42, 63, 55],
                    [43, 66, 58],
                    [44, 67, 59],
                    [45, 69, 61],
                    [46, 69, 61],
                    [47, 70, 62],
                    [48, 72, 64],
                    [49, 68, 60],
                    [50, 66, 58],
                    [51, 65, 57],
                    [52, 67, 59],
                    [53, 70, 62],
                    [54, 71, 63],
                    [55, 72, 64],
                    [56, 73, 65],
                    [57, 75, 67],
                    [58, 70, 62],
                    [59, 68, 60],
                    [60, 64, 56],
                    [61, 60, 52],
                    [62, 65, 57],
                    [63, 67, 59],
                    [64, 68, 60],
                    [65, 69, 61],
                    [66, 70, 62],
                    [67, 72, 64],
                    [68, 75, 67],
                    [69, 80, 72]
                ]);

                var options = {
                    hAxis: {
                        title: 'Time',
                        logScale: true
                    },
                    vAxis: {
                        title: 'Popularity',
                        logScale: false
                    },
                    colors: ['#a52714', '#097138']
                };

                var chart = new google.visualization.LineChart(document.getElementById('coreChart_div'));
                chart.draw(data, options);
            }*/

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
                data.addColumn('number', 'X');
                var keysArray = [];
                angular.forEach(response, function(value, key) {
                    data.addColumn('number', key);
                    keysArray.push(key);
                });
                var size = response.size;
                data.addRows(100);
                /*
                                      for(i = 0; i < 100; i++) {
                                          data.setCell(i ,0, i)
                                          data.setCell(i ,1, getStatusCode(response[keysArray[0]][i].result))
                                          data.setCell(i ,2, .5+getStatusCode(response[keysArray[1]][i].result))
                                          data.setCell(i ,3, 1+getStatusCode(response[keysArray[2]][i].result))
                                      }
                */

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
                        logScale: false
                    },
                    /*
                                            vAxis: {
                                              title: 'Status',
                                              logScale: false
                                            },
                    */
                    colors: ['#097138', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360', '#97BBCD'],
                    smoothLine: true,
                    animation: {
                        startup: true
                    },
                    chartArea:{left:20,top:15,width:'65%',height:'75%'}
                    //                        legend: { position: 'bottom' }
                };

                var chart = new google.visualization.LineChart(document.getElementById('coreChart_div'));
                chart.draw(data, options);
            }

            function getStatusCode(job) {
                var status = job.result
                if (status === "SUCCESS") return 1
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