import { Component } from '@angular/core';
import { RequestService } from 'process/api/request.service';
import { EnvironmentHelperService } from 'app/services/environment.helper.service';
import { Router } from '@angular/router';
import { Environment } from 'process/model/environment';

@Component({
  selector: 'app-start',
  templateUrl: './start.component.html',
  styleUrls: [ '../tasks.component.scss' ]
})
export class StartComponent {

  requestId = '3';
  createAnotherRequest = false;
  environment: Environment = this.envProvider.none();

  constructor(
    private client: RequestService,
    private envProvider: EnvironmentHelperService,
    private router: Router
  ) {
    this.envProvider.env().subscribe(e => this.environment = e);
  }

  start() {
    console.log('Starting new approval process');
    this.client.start(this.requestId, 'kermit').subscribe(
      result => {
        console.log('Sucessfully submitted');
        if (! this.createAnotherRequest) {
          this.tasklist();
        }
      }, error => {
        console.log('Error starting new process');
      }
    );
  }

  tasklist() {
    this.router.navigate(['/externalRedirect', { externalUrl: this.environment.tasklistUrl }], {
      skipLocationChange: true,
    });
  }


}
