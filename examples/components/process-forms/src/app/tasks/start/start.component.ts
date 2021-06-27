import { Component } from '@angular/core';
import { RequestService } from 'process/services/request.service';
import { EnvironmentHelperService } from 'app/services/environment.helper.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Environment } from 'process/models/environment';
import * as ApprovalRequestDraftSamples from 'app/data/approval-request-draft';


@Component({
  selector: 'app-start',
  templateUrl: './start.component.html',
  styleUrls: ['../tasks.component.scss']
})
export class StartComponent {

  approvalRequestDraft = ApprovalRequestDraftSamples.empty;

  createAnotherRequest = false;
  environment: Environment = this.envProvider.none();
  startSuccess = false;
  startFailure = false;
  userId: string;
  valid = true;

  constructor(
    private client: RequestService,
    private envProvider: EnvironmentHelperService,
    private router: Router,
    route: ActivatedRoute
  ) {
    this.userId = route.snapshot.queryParams['userId'];
    this.envProvider.env().subscribe(e => this.environment = e);
  }

  start() {
    console.log('Starting new approval process by' + this.userId);
    this.startSuccess = false;
    this.startFailure = false;

    // read it out of local storage.
    this.client.startNewApproval({
      'X-Current-User-ID': this.userId,
      revision: '1',
      body: this.approvalRequestDraft
    }).subscribe(
      result => {
        console.log('Successfully submitted');
        this.startSuccess = true;
        if (!this.createAnotherRequest) {
          this.tasklist();
        }
      }, error => {
        console.log('Error starting new process');
        this.startFailure = true;
      }
    );
  }

  validate($event) {
    this.valid = $event.valid;
  }


  tasklist() {
    this.router.navigate(['/externalRedirect', { externalUrl: this.environment.tasklistUrl }], {
      skipLocationChange: true
    });
  }

}
