<template>
  <Row style="padding:20px">
    <Spin size="large" fix v-if="isLoading">
      <Icon type="ios-loading" size="44" class="spin-icon-load"></Icon>
      <div>加载中...</div>
    </Spin>
    <Col span="6">
      <Row>
        <Card>
          <p slot="title">{{ $t("upload_plugin_pkg_title") }}</p>
          <Upload
            show-upload-list
            accept=".zip"
            name="zip-file"
            :on-success="onSuccess"
            :on-error="onError"
            action="/plugin/upload"
            :headers="setUploadActionHeader"
          >
            <Button icon="ios-cloud-upload-outline">
              {{ $t("upload_plugin_btn") }}
            </Button>
          </Upload>
        </Card>
      </Row>
      <Row class="plugins-tree-container">
        <Card>
          <p slot="title">{{ $t("plugins_list") }}</p>
          <Tree :data="plugins" @on-select-change="selectPlugin"></Tree>
        </Card>
      </Row>
    </Col>
    <Col span="17" offset="1">
      <div v-if="Object.keys(currentPlugin).length > 0">
        <div v-if="currentPlugin.children">
          <Row class="instances-container">
            <Card>
              <p slot="title">实例</p>
              <Row>
                <Select
                  @on-change="selectHost"
                  multiple
                  style="width: 40%"
                  :max-tag-count="4"
                  v-model="selectHosts"
                >
                  <Option
                    v-for="item in allAvailiableHosts"
                    :value="item"
                    :key="item"
                    >{{ item }}</Option
                  >
                </Select>
                <Button
                  size="small"
                  type="success"
                  @click="getAvailablePortByHostIp"
                  >端口预览</Button
                >
                <div v-if="availiableHostsWithPort.length > 0">
                  <p style="margin-top: 20px">可用端口:</p>

                  <div
                    v-for="item in availiableHostsWithPort"
                    :key="item.ip + item.port"
                  >
                    <div
                      class="instance-item-container"
                      style="border-bottom: 1px solid gray; padding: 10px 0"
                    >
                      <div class="instance-item">
                        {{ item.ip + ":" + item.port }}
                      </div>
                      <span>启动参数:</span>
                      <Input
                        type="textarea"
                        style="width: 50%"
                        :autosize="true"
                        v-model="item.createParams"
                      />
                      <Button
                        size="small"
                        type="success"
                        @click="
                          createPluginInstanceByPackageIdAndHostIp(
                            item.ip,
                            item.port,
                            item.createParams
                          )
                        "
                        >{{ $t("create") }}</Button
                      >
                    </div>
                  </div>
                </div>
              </Row>
              <Row>
                <p style="margin-top: 20px">{{ $t("running_node") }}:</p>
                <div v-if="allInstances.length === 0">暂无运行节点</div>
                <div v-else>
                  <div v-for="item in allInstances" :key="item.id">
                    <div class="instance-item-container">
                      <div class="instance-item">{{ item.displayLabel }}</div>

                      <Button
                        size="small"
                        type="error"
                        @click="removePluginInstance(item.id)"
                        >{{ $t("ternmiante") }}</Button
                      >
                    </div>
                  </div>
                </div>
              </Row>
            </Card>

            <Card style="margin-top: 20px">
              <p slot="title">日志查询</p>
              <div style="padding: 0 0 50px 0">
                <WeTable
                  :tableData="tableData"
                  :tableInnerActions="innerActions"
                  :tableColumns="tableColumns"
                  :pagination="pagination"
                  @actionFun="actionFun"
                  @handleSubmit="handleSubmit"
                  @pageChange="pageChange"
                  @pageSizeChange="pageSizeChange"
                  :showCheckbox="false"
                  tableHeight="650"
                  ref="table"
                ></WeTable>
              </div>

              <Modal
                v-model="logDetailsModalVisible"
                title="日志详情"
                footer-hide
                width="70"
              >
                <div
                  lang="json"
                  style="white-space: pre-wrap;"
                  v-html="logDetails"
                ></div>
              </Modal>
            </Card>
          </Row>
        </div>
        <Form v-else :model="form">
          <Row>
            <Col span="6" offset="0">
              <FormItem :label-width="50" label="Plugin">
                <Input v-model="currentPlugin.name" disabled />
              </FormItem>
            </Col>
            <Col span="6" offset="1">
              <FormItem :label-width="100" label="CI Type">
                <Select
                  @on-change="selectCiType"
                  label-in-value
                  v-model="selectedCiType"
                >
                  <OptionGroup
                    v-for="ci in ciTypes"
                    :label="ci.value || '-'"
                    :key="ci.code"
                  >
                    <Option
                      v-for="item in ci.ciTypes"
                      :value="item.ciTypeId || ''"
                      :key="item.ciTypeId"
                      >{{ item.name }}</Option
                    >
                  </OptionGroup>
                </Select>
              </FormItem>
            </Col>
            <Col
              style="margin-top:-5px"
              span="6"
              offset="1"
              v-if="selectedCiType !== ''"
            >
              <refPayloadModal
                :ciRulesFilters="ciRulesFilters"
                @handleSubmit="getCIRulesPayload"
              ></refPayloadModal>
            </Col>
          </Row>
          <hr />
          <Row style="margin-bottom:10px;margin-top:10px">
            <Col span="3">
              <span>操作</span>
            </Col>
            <Col span="2">
              <span>参数类型</span>
            </Col>
            <Col span="3">
              <span>数据状态</span>
            </Col>
            <Col span="3">
              <span>参数名</span>
            </Col>
            <Col span="5" offset="1">
              <span>CMDB属性</span>
            </Col>
          </Row>
          <Row
            style="margin-top:20px; border-bottom: 1px solid #2c3e50"
            v-for="(interfaces, index) in pluginInterfaces"
            :key="interfaces.id"
          >
            <Col span="3">
              <Tooltip :content="interfaces.name">
                <span
                  style="display: inline-block;max-width: 95%;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                >
                  {{ interfaces.name }}
                </span>
              </Tooltip>
            </Col>
            <Col span="21">
              <Row>
                <Col span="2">
                  <FormItem :label-width="0">
                    <span>输入参数</span>
                  </FormItem>
                </Col>
                <Col span="3">
                  <FormItem :label-width="0">
                    <Select v-model="interfaces.filterStatus" clearable>
                      <Option
                        v-for="item in ciStatus"
                        :value="item.value.toString()"
                        :key="item.value"
                        >{{ item.label }}</Option
                      >
                    </Select>
                  </FormItem>
                </Col>
                <Col span="17" offset="1">
                  <Row
                    v-for="param in interfaces.inputParameters"
                    :key="param.id"
                  >
                    <Col span="5">
                      <FormItem :label-width="0">
                        <Tooltip :content="param.name">
                          <span
                            style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                          >
                            {{ param.name }}
                          </span>
                        </Tooltip>
                      </FormItem>
                    </Col>
                    <Col span="18">
                      <FormItem :label-width="0">
                        <!-- <AttrInput
                          :allCiTypes="ciTypes"
                          :cmdbColumnSource="param.cmdbColumnSource"
                          :rootCiType="selectedCiType"
                          v-model="param.cmdbAttr"
                          :ciTypesObj="ciTypesObj"
                          :ciTypeAttributeObj="ciTypeAttributeObj"
                        /> -->
                        <CmdbAttrInput
                          :allCodes="allCodes"
                          :allCiTypes="ciTypes"
                          :paramData="param"
                          :rootCiType="selectedCiType"
                          v-model="param.cmdbAttr"
                          :ciTypesObj="ciTypesObj"
                          :ciTypeAttributeObj="ciTypeAttributeObj"
                        />
                      </FormItem>
                    </Col>
                  </Row>
                </Col>
              </Row>
              <Row>
                <Col span="2">
                  <FormItem :label-width="0">
                    <span>输出参数</span>
                  </FormItem>
                </Col>
                <Col span="3">
                  <FormItem :label-width="0">
                    <Select v-model="interfaces.resultStatus" clearable>
                      <Option
                        v-for="item in ciStatus"
                        :value="item.value.toString()"
                        :key="item.value"
                        >{{ item.label }}</Option
                      >
                    </Select>
                  </FormItem>
                </Col>
                <Col span="17" offset="1">
                  <Row
                    v-for="outPut in interfaces.outputParameters"
                    :key="outPut.id + 1000"
                  >
                    <Col span="5">
                      <FormItem :label-width="0">
                        <Tooltip :content="outPut.name">
                          <span
                            style="display: inline-block;white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                          >
                            {{ outPut.name }}
                          </span>
                        </Tooltip>
                      </FormItem>
                    </Col>
                    <Col span="18">
                      <FormItem :label-width="0">
                        <Select
                          style="width:150px"
                          placeholder="请选择"
                          v-model="outPut.cmdbColumnSource"
                          clearable
                        >
                          <Option
                            v-for="attr in currentCiTyPeAttr"
                            :key="attr.propertyName"
                            :value="attr.propertyName"
                            :label="attr.name"
                          ></Option>
                        </Select>
                      </FormItem>
                    </Col>
                  </Row>
                </Col>
              </Row>
            </Col>
          </Row>
          <Row style="margin:20px auto">
            <Col span="5" offset="10">
              <Button
                type="primary"
                v-if="
                  currentPlugin.status === 'NOT_CONFIGURED' ||
                    currentPlugin.status === 'CONFIGURED' ||
                    currentPlugin.status === 'DECOMMISSIONED'
                "
                @click="pluginSave"
                >保存</Button
              >
              <Button
                type="primary"
                v-if="
                  currentPlugin.status === 'CONFIGURED' ||
                    currentPlugin.status === 'DECOMMISSIONED'
                "
                @click="regist"
                >注册</Button
              >
              <Button
                type="error"
                v-if="currentPlugin.status === 'ONLINE'"
                @click="removePlugin"
                >注销</Button
              >
            </Col>
          </Row>
        </Form>
      </div>
    </Col>
  </Row>
</template>
<script>
import AttrSelect from "../components/attr-select";
import AttrInput from "../components/attr-input";
import CmdbAttrInput from "../components/cmdb-attr-input";
import {
  getAllCITypesByLayerWithAttr,
  getAllPluginPkgs,
  getPluginInterfaces,
  getRefCiTypeFrom,
  getRefCiTypeTo,
  getCiTypeAttr,
  getAllInstancesByPackageId,
  createPluginInstanceByPackageIdAndHostIp,
  removePluginInstance,
  savePluginInstance,
  queryLog,
  getPluginInstanceLogDetail,
  getCiTypeAttrRefAndSelect,
  getEnumCodesByCategoryId,
  getAllSystemEnumCodes,
  decommissionPluginConfig,
  releasePluginConfig,
  getAvailableContainerHosts,
  getAvailablePortByHostIp,
  preconfigurePluginPackage
} from "@/api/server.js";

import refPayloadModal from "./components/ref-payload-modal.js";

const innerActions = [
  {
    label: "显示详情",
    props: {
      type: "info",
      size: "small"
    },
    actionType: "showLogDetails"
  }
];

export default {
  components: {
    AttrSelect,
    AttrInput,
    refPayloadModal,
    CmdbAttrInput
  },
  data() {
    return {
      allCodes: [],
      ciTypesObj: {},
      ciTypeAttributeObj: {},
      allAvailiableHosts: [],
      selectHosts: [],
      availiableHostsWithPort: [],
      ciStatus: [],
      plugins: [],
      ciTypes: [],
      pluginInterfaces: [],
      selectedCiType: "",
      selectedCiTypeIdAndName: {},
      currentPlugin: {},
      currentCiTyPeAttr: [],
      allInstances: [],
      currentPackageId: 0,
      currentInstanceId: 0,
      createIpValue: "",
      form: {},
      tableData: [],
      totalTableData: [],
      innerActions,
      tableColumns: [
        {
          title: "插件运行实例",
          key: "instance",
          inputKey: "instance",
          searchSeqNo: 1,
          displaySeqNo: 1,
          component: "WeSelect",
          isMultiple: true,
          placeholder: "插件运行实例",
          span: 5,
          width: "200px",
          options: []
        },
        {
          title: "文件名",
          key: "file_name",
          inputKey: "file_name",
          searchSeqNo: 2,
          displaySeqNo: 2,
          component: "Input",
          isNotFilterable: true,
          placeholder: "文件名",
          width: "200px"
        },
        {
          title: "行号",
          key: "line_number",
          inputKey: "line_number",
          searchSeqNo: 3,
          displaySeqNo: 3,
          component: "Input",
          isNotFilterable: true,
          placeholder: "行号",
          width: "100px"
        },
        {
          title: "匹配内容",
          key: "log",
          inputKey: "log",
          searchSeqNo: 4,
          displaySeqNo: 4,
          component: "Input",
          placeholder: "支持正则表达式"
        }
      ],
      pagination: {
        pageSize: 10,
        currentPage: 1,
        total: 0
      },
      searchFilters: [],
      logDetailsModalVisible: false,
      logDetails: "",
      ciRulesFilters: [],
      ciRulesPayload: [],
      isLoading: false,
      defaultCreateParams: ""
    };
  },
  watch: {
    currentPlugin: {
      handler(val) {},
      deep: true
    },
    selectedCiType: {
      handler(val) {
        this.selectCiType({ value: val });
      },
      immediate: true
    }
  },
  methods: {
    async getAllCITypesByLayerWithAttr() {
      let { status, data, message } = await getAllCITypesByLayerWithAttr([
        "notCreated",
        "created",
        "dirty",
        "decommissioned"
      ]);
      if (status === "OK") {
        let ciTypes = {};
        let ciTypeAttrs = {};

        let tempCITypes = JSON.parse(JSON.stringify(data));
        tempCITypes.forEach(_ => {
          _.ciTypes && _.ciTypes.filter(i => i.status !== "decommissioned");
        });
        this.ciTypes = tempCITypes;

        data.forEach(layer => {
          if (layer.ciTypes instanceof Array) {
            layer.ciTypes.forEach(citype => {
              ciTypes[citype.ciTypeId] = citype;
              if (citype.attributes instanceof Array) {
                citype.attributes.forEach(citypeAttr => {
                  ciTypeAttrs[citypeAttr.ciTypeAttrId] = citypeAttr;
                });
              }
            });
          }
        });
        this.ciTypesObj = ciTypes;
        this.ciTypeAttributeObj = ciTypeAttrs;
      }
    },
    pageChange(current) {
      this.pagination.currentPage = current;
      this.handlePaginationByFE();
    },
    pageSizeChange(size) {
      this.pagination.pageSize = size;
      this.handlePaginationByFE();
    },
    actionFun(type, data) {
      if (type === "showLogDetails") {
        this.getLogDetail(data);
      }
    },
    getCIRulesPayload(data) {
      this.ciRulesPayload = [];
      for (let i in data) {
        this.ciRulesPayload.push({
          cmdbAttributeId: i,
          filteringValues: data[i]
        });
      }
    },
    async getCiStatus(id) {
      // getAllSystemEnumCodes
      const payload = {
        filters: [{ name: "cat.catId", operator: "eq", value: id }]
      };
      let { data, status, message } = await getAllSystemEnumCodes(payload);
      if (status === "OK") {
        this.ciStatus = data.contents.map(_ => {
          return {
            value: _.codeId,
            label: _.value || ""
          };
        });
      }
    },
    async getAllCodes() {
      // getAllSystemEnumCodes
      const payload = {
        filters: [],
        paging: false
      };
      let { data, status, message } = await getAllSystemEnumCodes(payload);
      if (status === "OK") {
        this.allCodes = data.contents
          .map(_ => {
            return {
              codeId: _.codeId,
              value: _.codeId,
              label: _.value || "",
              catName: _.cat.catName
            };
          })
          .filter(i => i.catName != "tab_query_of_architecture_design")
          .filter(j => j.catName != "tab_query_of_deploy_design");
      }
    },
    async getLogDetail(logData) {
      const payload = {
        inputs: [
          {
            file_name: logData.file_name,
            line_number: logData.line_number,
            relate_line_count: 10
          }
        ]
      };

      let { data, status, message } = await getPluginInstanceLogDetail(
        logData.instanceId,
        payload
      );
      if (status === "OK") {
        this.logDetailsModalVisible = true;
        let re = new RegExp(this.searchFilters[1].value, "g");
        this.logDetails = data.outputs[0].logs
          .toString()
          .replace(
            re,
            `<span style="background-color: #ff0">${
              this.searchFilters[1].value
            }</span>`
          );
      }
    },
    handleSubmit(data) {
      this.searchFilters = data;
      this.getTableData();
    },
    async regist() {
      const saveRes = await this.pluginSave();
      if (saveRes.status === "OK") {
        let { status, data, message } = await releasePluginConfig(
          this.currentPlugin.id
        );
        if (status === "OK") {
          this.$Notice.success({
            title: "Success",
            desc: message
          });
          this.queryPluginAffterUpdate();
        }
      }
    },
    async pluginSave() {
      let payload = [];
      payload = this.pluginInterfaces.map(i => {
        return {
          interfaceFilterStatus: i.filterStatus
            ? i.filterStatus.toString()
            : "",
          interfaceResultStatus: i.resultStatus
            ? i.resultStatus.toString()
            : "",
          inputParameterMappings: i.inputParameters.map(inp => {
            const params = inp.cmdbAttr.cmdbColumnCriteria;
            const routine = params
              ? params.routine.length === 1
                ? params.routine
                : params.routine.slice(0, -1)
              : null;
            return {
              routine: routine,
              mappingType: inp.cmdbAttr.mappingType,
              cmdbEnumCode: inp.cmdbAttr.cmdbEnumCode,
              cmdbAttributeId: params ? params.attribute.attrId : null,
              cmdbCiTypeId:
                routine &&
                routine[routine.length - 1] &&
                routine[routine.length - 1].ciTypeId,
              cmdbColumnSource: inp.cmdbAttr.cmdbColumnSource
                ? JSON.stringify(inp.cmdbAttr.cmdbColumnSource)
                : null,
              parameterId: inp.id
            };
          }),
          interfaceId: i.id.toString(),
          interfaceName: i.name,
          outputParameterMappings: i.outputParameters.map(out => {
            return {
              cmdbColumnSource: out.cmdbColumnSource,
              parameterId: out.id
            };
          })
        };
      });
      const requestData = {
        cmdbCiTypeId: this.selectedCiTypeIdAndName.value,
        cmdbCiTypeName: this.selectedCiTypeIdAndName.label,
        configId: this.currentPlugin.id,
        pluginRegisteringModels: {
          filteringRuleConfigs: this.ciRulesPayload,
          interfaceConfigs: payload
        }
      };
      let { status, data, message } = await savePluginInstance(requestData);
      if (status === "OK") {
        this.$Notice.success({
          title: "Success",
          desc: message
        });
        this.queryPluginAffterUpdate();
      }
      return { status, data, message };
    },
    async removePlugin() {
      let { status, data, message } = await decommissionPluginConfig(
        this.currentPlugin.id
      );
      if (status === "OK") {
        this.$Notice.success({
          title: "Success",
          desc: message
        });
        this.queryPluginAffterUpdate();
      }
    },
    async queryPluginAffterUpdate() {
      let { status, data, message } = await getAllPluginPkgs();
      if (status === "OK") {
        this.plugins = data.map(_ => {
          return {
            ..._,
            title: `${_.name}[${_.version}]`,
            id: _.id,
            expand: false,
            checked: false,
            children: _.pluginConfigs.map(i => {
              return {
                ...i,
                title: i.name,
                id: i.id,
                expand: true,
                checked: false
              };
            })
          };
        });
        this.plugins.forEach(_ => {
          const found = _.pluginConfigs.find(
            p => p.id === this.currentPlugin.id
          );
          if (found) {
            this.currentPlugin = found;
            return;
          }
        });
      }
    },
    async selectCiType(c) {
      if (c && c.value) {
        this.pluginInterfaces.forEach(_ => {
          _.inputParameters.forEach(inparams => {
            this.$set(inparams, "cmdbAttr", inparams);
          });
          // _.outputParameters.forEach(outparams => {
          //   this.$set(outparams, "cmdbColumnSource", _.cmdbColumnSource||'');
          // });
        });
        this.selectedCiTypeIdAndName = c;
        const ciTypeAttr = await getCiTypeAttr(c.value);
        if (ciTypeAttr.status === "OK") {
          this.currentCiTyPeAttr = ciTypeAttr.data;
          const statusId = ciTypeAttr.data.find(
            attr => attr.propertyName === "state"
          ).referenceId;
          this.getCiStatus(statusId);
        }
        this.setCiRulesFilters(c.value);
      }
    },
    async setCiRulesFilters(id) {
      let getRefAndSelectData = refAndSelectData => {
        this.ciRulesFilters = [];
        refAndSelectData.forEach(async (item, index) => {
          if (item.inputType === "select") {
            const { status, message, data } = await getEnumCodesByCategoryId(
              0,
              item.referenceId
            );

            item["options"] = data
              .filter(j => j.status === "active")
              .map(i => {
                return {
                  label: i.code,
                  value: i.codeId
                };
              });

            this.ciRulesFilters.push(item);
          } else {
            this.ciRulesFilters.push(item);
          }
        });
      };

      let refAndSelectRes = await getCiTypeAttrRefAndSelect(id);
      if (refAndSelectRes.status === "OK") {
        getRefAndSelectData(refAndSelectRes.data);
      }
    },
    async selectPlugin(plugins, currentPlugin) {
      this.selectedCiType = currentPlugin.cmdbCiTypeId || "";
      this.currentPlugin = currentPlugin;
      let { status, data, message } = await getPluginInterfaces(
        currentPlugin.id
      );
      if (status === "OK") {
        this.pluginInterfaces = data.map(d => {
          return {
            ...d,
            inputParameters: d.inputParameters.sort((a, b) => a.id - b.id),
            outputParameters: d.outputParameters.sort((a, b) => a.id - b.id)
          };
        });
        this.defaultCreateParams = currentPlugin.containerStartParam;
      }
      const ciTypeId = currentPlugin.cmdbCiTypeId;
      if (ciTypeId) {
        this.setCiRulesFilters(ciTypeId);
        this.ciTypes.forEach(_ => {
          const found =
            _.ciTypes && _.ciTypes.find(c => c.ciTypeId === ciTypeId);
          if (found) {
            this.selectedCiTypeIdAndName = {
              value: found.ciTypeId,
              label: found.name
            };
          }
        });
        const ciTypeAttr = await getCiTypeAttr(ciTypeId);
        this.currentCiTyPeAttr = ciTypeAttr.data;
      }

      if (currentPlugin.pluginConfigs) {
        this.selectHosts = [];
        this.availiableHostsWithPort = [];
        this.currentPackageId = currentPlugin.id;
        this.getAllInstancesByPackageId(this.currentPackageId);
      }
      this.getAvailableContainerHosts();
      this.resetLogTable();
    },
    resetLogTable() {
      this.tableData = [];
      this.totalTableData = [];
      this.$refs.table && this.$refs.table.reset();
    },
    async getAvailableContainerHosts() {
      const { data, status, message } = await getAvailableContainerHosts();
      if (status === "OK") {
        this.allAvailiableHosts = data;
      }
    },
    selectHost(v) {
      this.selectHosts = v;
    },
    getAvailablePortByHostIp() {
      this.availiableHostsWithPort = [];
      this.selectHosts.forEach(async _ => {
        const { data, status, message } = await getAvailablePortByHostIp(_);
        if (status === "OK") {
          this.availiableHostsWithPort.push({
            ip: _,
            port: data,
            createParams: this.defaultCreateParams
          });
        }
      });
    },
    async createPluginInstanceByPackageIdAndHostIp(ip, port, createParams) {
      let errorFlag = false;
      if (createParams.indexOf("{{") >= 0 || createParams.indexOf("}}") >= 0) {
        this.$Notice.warning({
          title: "Warning",
          desc: "请替换启动参数中的差异化参数（例如：{{parameter}}）"
        });
        errorFlag = true;
      }
      if (errorFlag) return;
      this.isLoading = true;
      const payload = {
        additionalCreateContainerParameters: createParams
      };
      const {
        data,
        status,
        message
      } = await createPluginInstanceByPackageIdAndHostIp(
        this.currentPlugin.id,
        ip,
        port,
        payload
      );
      if (status === "OK") {
        this.getAllInstancesByPackageId(this.currentPackageId);
      }
      this.isLoading = false;
    },
    async getAllInstancesByPackageId(id) {
      this.isLoading = true;
      let { data, status, message } = await getAllInstancesByPackageId(id);
      if (status === "OK") {
        this.allInstances = data.map(_ => {
          if (_.status !== "REMOVED") {
            return {
              id: _.id,
              hostIp: _.host,
              port: _.port,
              displayLabel: _.host + ":" + _.port
            };
          }
        });
        this.getHostsForTableFilter();
      }
      this.isLoading = false;
    },
    getHostsForTableFilter() {
      this.tableColumns[0].options = this.allInstances.map(_ => {
        return {
          label: _.hostIp + ":" + _.port,
          value: _.id
        };
      });
    },
    async removePluginInstance(instanceId) {
      let { data, status, message } = await removePluginInstance(instanceId);
      if (status === "OK") {
        this.$Notice.success({
          title: "Success",
          desc: message
        });
        this.getAllInstancesByPackageId(this.currentPackageId);
      }
    },
    async getTableData() {
      if (this.searchFilters.length < 2) return;
      const payload = {
        instanceIds: this.searchFilters[0].value,
        pluginRequest: {
          inputs: [
            {
              key_word: this.searchFilters[1].value
            }
          ]
        }
      };
      let { status, data, message } = await queryLog(payload);
      if (status === "OK") {
        for (let i in data) {
          let arr = [];
          this.totalTableData = arr.concat(
            data[i].outputs.map(_ => {
              return {
                instance: this.allInstances.find(j => j.id === +i).displayLabel,
                instanceId: i,
                ..._
              };
            })
          );
        }

        this.handlePaginationByFE();
      }
    },
    handlePaginationByFE() {
      this.pagination.total = this.totalTableData.length;
      let temp = Array.from(this.totalTableData);
      this.tableData = temp.splice(
        (this.pagination.currentPage - 1) * this.pagination.pageSize,
        this.pagination.pageSize
      );
    },
    async getAllPluginPkgs() {
      let { status, data, message } = await getAllPluginPkgs();
      if (status === "OK") {
        this.plugins = data.map(_ => {
          return {
            ..._,
            title: `${_.name}[${_.version}]`,
            id: _.id,
            expand: false,
            checked: false,
            children: _.pluginConfigs.map(i => {
              return {
                ...i,
                title: i.name,
                id: i.id,
                expand: true,
                checked: false
              };
            })
          };
        });
      }
    },

    async onSuccess(response, file, filelist) {
      this.$Notice.success({
        title: "Success",
        desc: response.message || ""
      });
      if (response.status === "OK") {
        const { message, status, data } = await preconfigurePluginPackage(
          response.data.id
        );
        this.getAllPluginPkgs();
      }
    },
    onError(error, file, filelist) {
      this.$Notice.error({
        title: "Error",
        desc: file.message || ""
      });
    }
  },

  created() {
    this.getAllCITypesByLayerWithAttr();
    this.getAllPluginPkgs();
  },
  mounted() {
    this.getAllCodes();
  },
  computed: {
    setUploadActionHeader() {
      let uploadToken = document.cookie
        .split(";")
        .find(i => i.indexOf("XSRF-TOKEN") !== -1);
      return {
        "X-XSRF-TOKEN": uploadToken && uploadToken.split("=")[1]
      };
    }
  }
};
</script>

<style lang="scss" scoped>
.plugins-tree-container {
  margin-top: 30px;
}

.instances-container {
  height: 300px;
  .instance-query-input {
    width: 200px;
    margin-right: 20px;
  }
  .instance-item-container {
    line-height: 30px;
    overflow-y: auto;

    .instance-item {
      width: 220px;
      display: inline-block;
    }
  }
}

.result-container {
  max-height: 700px;
  overflow-y: auto;
}
.ivu-form-item {
  margin-bottom: 10px;
  margin-top: -6px;
}
</style>
