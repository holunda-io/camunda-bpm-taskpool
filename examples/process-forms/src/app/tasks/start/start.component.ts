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
  startSuccess = false;
  startFailure = false;

  constructor(
    private client: RequestService,
    private envProvider: EnvironmentHelperService,
    private router: Router
  ) {
    this.envProvider.env().subscribe(e => this.environment = e);
  }

  start() {
    console.log('Starting new approval process');
    this.startSuccess = false;
    this.startFailure = false;
    // FIXME: smell, the userIdentifier of the originator should not be hard-coded here.
    // read it out of local storage.
    this.client.start(this.requestId, 'ironman').subscribe(
      result => {
        console.log('Successfully submitted');
        this.startSuccess = true;
        if (! this.createAnotherRequest) {
          this.tasklist();
        }
      }, error => {
        console.log('Error starting new process');
        this.startFailure = true;
      }
    );
  }

  tasklist() {
    this.router.navigate(['/externalRedirect', { externalUrl: this.environment.tasklistUrl }], {
      skipLocationChange: true,
    });
  }


}
