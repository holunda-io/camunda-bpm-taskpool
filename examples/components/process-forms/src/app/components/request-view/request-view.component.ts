import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import * as ApprovalRequestDraftSamples from 'app/data/approval-request-draft';
import {ApprovalRequestDraft} from 'process/model/approvalRequestDraft';
@Component({
  selector: 'app-request-view',
  templateUrl: './request-view.component.html',
  styleUrls: []
})
export class RequestViewComponent {

  @Input()
  approvalRequest: ApprovalRequestDraft;

}
